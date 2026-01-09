package com.gaming.publisher.consumer;

import com.gaming.publisher.dto.GameCrashReportedEvent;
import com.gaming.publisher.model.CrashReport;
import com.gaming.publisher.repository.CrashReportRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Consommateur Kafka pour les événements de crash de jeu.
 *
 * RESPONSABILITÉ : Écouter le topic "game-crash-reported" et stocker
 * les rapports de crash en base de données pour analyse.
 *
 * FONCTIONNEMENT :
 * 1. S'abonne au topic au démarrage (@PostConstruct)
 * 2. Poll les messages en continu dans un thread séparé
 * 3. Pour chaque crash reçu, l'enregistre en base
 * 4. Log une alerte si le nombre de crashs dépasse le seuil
 *
 * NOTE: Temporairement désactivé - décommenter @Component pour activer
 */
// @Component  // TODO: Décommenter quand Kafka est configuré
public class GameCrashConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GameCrashConsumer.class);

    private final KafkaConsumer<String, GameCrashReportedEvent> consumer;
    private final CrashReportRepository crashReportRepository;
    private final ExecutorService executorService;
    private final String topicName;
    private final long crashThreshold;

    private volatile boolean running = true;

    /**
     * Constructeur avec injection de dépendances.
     *
     * @param crashConsumerConfigs Configuration Kafka spécifique pour ce consumer
     * @param crashReportRepository Repository pour sauvegarder les crashs
     * @param topicName Nom du topic à écouter
     * @param crashThreshold Seuil d'alerte
     */
    public GameCrashConsumer(
            @Qualifier("crashConsumerConfigs") Map<String, Object> crashConsumerConfigs,
            CrashReportRepository crashReportRepository,
            @Value("${kafka.topic.game-crash-reported}") String topicName,
            @Value("${publisher.crash-threshold}") long crashThreshold) {

        this.consumer = new KafkaConsumer<>(crashConsumerConfigs);
        this.crashReportRepository = crashReportRepository;
        this.topicName = topicName;
        this.crashThreshold = crashThreshold;
        this.executorService = Executors.newSingleThreadExecutor();

        logger.info("GameCrashConsumer initialisé pour le topic: {}", topicName);
    }

    /**
     * Démarre la consommation au lancement de l'application.
     *
     * LIFECYCLE : Appelé automatiquement après la construction du bean.
     */
    @PostConstruct
    public void start() {
        // S'abonne au topic
        consumer.subscribe(Collections.singletonList(topicName));

        // Lance la boucle de consommation dans un thread séparé
        executorService.submit(this::consume);

        logger.info("GameCrashConsumer démarré et en écoute sur: {}", topicName);
    }

    /**
     * Boucle principale de consommation des messages.
     *
     * FONCTIONNEMENT :
     * 1. Poll Kafka toutes les 100ms
     * 2. Pour chaque message reçu, traite le crash
     * 3. Continue jusqu'à l'arrêt de l'application
     */
    private void consume() {
        try {
            while (running) {
                // Poll des nouveaux messages (timeout 100ms)
                ConsumerRecords<String, GameCrashReportedEvent> records =
                    consumer.poll(Duration.ofMillis(100));

                // Traite chaque message reçu
                for (ConsumerRecord<String, GameCrashReportedEvent> record : records) {
                    handleCrashReport(record.value());
                }
            }
        } catch (Exception e) {
            logger.error("Erreur dans la boucle de consommation: {}", e.getMessage(), e);
        } finally {
            consumer.close();
            logger.info("GameCrashConsumer arrêté");
        }
    }

    /**
     * Traite un rapport de crash reçu.
     *
     * LOGIQUE MÉTIER :
     * 1. Convertit l'événement Kafka en entité JPA
     * 2. Sauvegarde en base de données
     * 3. Vérifie si le seuil d'alerte est dépassé
     *
     * @param event Événement de crash reçu
     */
    private void handleCrashReport(GameCrashReportedEvent event) {
        try {
            logger.info("Crash reçu pour le jeu {} - Code: {}",
                event.getGameTitle(), event.getErrorCode());

            // Convertit l'événement en entité JPA
            CrashReport crashReport = CrashReport.builder()
                .crashId(event.getCrashId())
                .gameId(event.getGameId())
                .gameTitle(event.getGameTitle())
                .errorCode(event.getErrorCode())
                .errorMessage(event.getErrorMessage())
                .stackTrace(event.getStackTrace())
                .platform(event.getPlatform())
                .gameVersion(event.getGameVersion())
                .crashTimestamp(timestampToLocalDateTime(event.getCrashTimestamp()))
                .userId(event.getUserId())
                .build();

            // Sauvegarde en base
            crashReportRepository.save(crashReport);

            // Vérifie le seuil d'alerte
            checkCrashThreshold(event.getGameId(), event.getGameTitle());

        } catch (Exception e) {
            logger.error("Erreur lors du traitement du crash {}: {}",
                event.getCrashId(), e.getMessage(), e);
        }
    }

    /**
     * Vérifie si le nombre de crashs dépasse le seuil d'alerte.
     *
     * ALERTING : Si le seuil est dépassé, log un message WARN.
     * Dans un vrai système, on enverrait un email/Slack/PagerDuty.
     *
     * @param gameId ID du jeu
     * @param gameTitle Titre du jeu
     */
    private void checkCrashThreshold(String gameId, String gameTitle) {
        long crashCount = crashReportRepository.countByGameId(gameId);

        if (crashCount > crashThreshold) {
            logger.warn("⚠️ ALERTE PATCH URGENT ! Le jeu '{}' a {} crashs (seuil: {})",
                gameTitle, crashCount, crashThreshold);
        }
    }

    /**
     * Utilitaire de conversion timestamp -> LocalDateTime.
     *
     * @param timestamp Timestamp en millisecondes (epoch)
     * @return LocalDateTime correspondant
     */
    private LocalDateTime timestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        );
    }

    /**
     * Arrête proprement le consommateur avant la fermeture de l'application.
     *
     * LIFECYCLE : Appelé automatiquement par Spring lors du shutdown.
     */
    @PreDestroy
    public void stop() {
        logger.info("Arrêt du GameCrashConsumer...");
        running = false;
        executorService.shutdown();
    }
}

