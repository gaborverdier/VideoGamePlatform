package com.gaming.platform.consumer;

import com.gaming.events.UserRegistered;
import com.gaming.platform.model.User;
import com.gaming.platform.repository.UserRepository;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Consommateur Kafka pour les événements d'inscription d'utilisateurs.
 *
 * RESPONSABILITÉ : Écouter le topic "user-registered" et créer
 * les utilisateurs dans la base de données de la plateforme.
 *
 * FONCTIONNEMENT :
 * 1. S'abonne au topic au démarrage (@PostConstruct)
 * 2. Poll les messages en continu dans un thread séparé
 * 3. Pour chaque utilisateur enregistré, l'ajoute en base
 * 4. Log les inscriptions reçues
 */
@Component
public class UserRegisteredConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final KafkaConsumer<String, UserRegistered> consumer;
    private final UserRepository userRepository;
    private final ExecutorService executorService;
    private final String topicName;

    private volatile boolean running = true;

    public UserRegisteredConsumer(
            UserRepository userRepository,
            @Value("${kafka.bootstrap.servers}") String bootstrapServers,
            @Value("${kafka.schema.registry.url}") String schemaRegistryUrl,
            @Value("${kafka.consumer.group-id}") String consumerGroupId,
            @Value("${kafka.topic.user-registered}") String topicName) {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-user-registered");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.userRepository = userRepository;
        this.topicName = topicName;
        this.executorService = Executors.newSingleThreadExecutor();

        logger.info("UserRegisteredConsumer initialisé pour le topic: {}", topicName);
    }

    @PostConstruct
    public void start() {
        consumer.subscribe(Collections.singletonList(topicName));
        executorService.submit(this::consume);
        logger.info("UserRegisteredConsumer démarré et en écoute sur: {}", topicName);
    }

    /**
     * Boucle principale de consommation.
     */
    private void consume() {
        try {
            while (running) {
                ConsumerRecords<String, UserRegistered> records =
                    consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, UserRegistered> record : records) {
                    try {
                        processUserRegistered(record.value());
                    } catch (Exception e) {
                        logger.error("Erreur lors du traitement de l'événement UserRegistered: {}", e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur dans la boucle de consommation: {}", e.getMessage(), e);
        } finally {
            consumer.close();
            logger.info("UserRegisteredConsumer fermé");
        }
    }

    /**
     * Traite un événement d'inscription d'utilisateur.
     *
     * @param event L'événement reçu depuis Kafka
     */
    private void processUserRegistered(UserRegistered event) {
        logger.info("Utilisateur inscrit reçu: {} ({})", event.getUsername(), event.getUserId());

        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsById(event.getUserId().toString())) {
            logger.warn("⚠️ L'utilisateur {} existe déjà en base", event.getUserId());
            return;
        }

        // Créer l'entité User
        User user = User.builder()
            .userId(event.getUserId().toString())
            .username(event.getUsername().toString())
            .email(event.getEmail().toString())
            .registrationTimestamp(event.getRegistrationTimestamp())
            .build();

        // Sauvegarder en base
        userRepository.save(user);

        logger.info("Utilisateur {} ajouté à la plateforme", event.getUsername());
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Arrêt du UserRegisteredConsumer...");
        running = false;
        executorService.shutdown();
    }
}
