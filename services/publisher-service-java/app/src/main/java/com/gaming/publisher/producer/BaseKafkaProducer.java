package com.gaming.publisher.producer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Classe abstraite de base pour tous les producteurs Kafka.
 *
 * PRINCIPE DRY : Évite la duplication du code de production Kafka
 * en centralisant la logique commune dans une classe de base.
 *
 * TEMPLATE METHOD PATTERN : Les sous-classes héritent de la logique
 * de production tout en pouvant personnaliser certains aspects.
 *
 * NOTE: Cette classe n'est PAS un @Component car elle est abstraite.
 * Seules les classes concrètes filles doivent être des @Component.
 *
 * REFACTORING: Construction de la config Kafka directement depuis les paramètres
 * pour éviter les références circulaires avec Spring beans.
 *
 * @param <T> Type de l'événement à produire (ex: GamePatchedEvent)
 */
public abstract class BaseKafkaProducer<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseKafkaProducer.class);

    protected final KafkaProducer<String, T> producer;
    protected final String topicName;

    /**
     * Constructeur qui initialise le producteur Kafka avec les paramètres individuels.
     * Cette approche évite l'injection du bean producerConfigs qui causait une référence circulaire.
     *
     * @param bootstrapServers Adresse du cluster Kafka (ex: localhost:9092)
     * @param schemaRegistryUrl URL du Schema Registry (ex: http://localhost:8081)
     * @param topicName Nom du topic Kafka
     */
    protected BaseKafkaProducer(String bootstrapServers, String schemaRegistryUrl, String topicName) {
        this.topicName = topicName;

        // Construction de la configuration Kafka
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        this.producer = new KafkaProducer<>(props);
        logger.info("Producteur Kafka initialisé pour le topic: {}", topicName);
    }

    /**
     * Publie un événement sur Kafka de manière asynchrone.
     *
     * FONCTIONNEMENT :
     * 1. Crée un ProducerRecord avec clé et valeur
     * 2. Envoie l'événement au broker Kafka
     * 3. Log le succès ou l'erreur
     *
     * @param key Clé du message (généralement l'ID du jeu)
     * @param event Événement à publier
     */
    public void sendAsync(String key, T event) {
        ProducerRecord<String, T> record = new ProducerRecord<>(topicName, key, event);

        // Envoi asynchrone avec callback
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                logger.error("Erreur lors de l'envoi du message sur {}: {}",
                    topicName, exception.getMessage(), exception);
            } else {
                logger.info("Message envoyé avec succès sur {} - Partition: {}, Offset: {}",
                    topicName, metadata.partition(), metadata.offset());
            }
        });
    }

    /**
     * Publie un événement sur Kafka de manière synchrone.
     *
     * DIFFÉRENCE avec sendAsync :
     * - sendAsync : retourne immédiatement, callback appelé plus tard
     * - sendSync : bloque jusqu'à la confirmation (plus lent mais plus sûr)
     *
     * @param key Clé du message
     * @param event Événement à publier
     * @return RecordMetadata contenant les infos de publication
     * @throws Exception si l'envoi échoue
     */
    public RecordMetadata sendSync(String key, T event) throws Exception {
        ProducerRecord<String, T> record = new ProducerRecord<>(topicName, key, event);

        try {
            // Envoi synchrone : attend la confirmation
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get();

            logger.info("Message envoyé en mode synchrone sur {} - Partition: {}, Offset: {}",
                topicName, metadata.partition(), metadata.offset());

            return metadata;
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi synchrone sur {}: {}",
                topicName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Ferme proprement le producteur Kafka.
     * Appelle flush() pour s'assurer que tous les messages sont envoyés.
     */
    public void close() {
        logger.info("Fermeture du producteur Kafka pour le topic: {}", topicName);
        producer.flush();
        producer.close();
    }
}

