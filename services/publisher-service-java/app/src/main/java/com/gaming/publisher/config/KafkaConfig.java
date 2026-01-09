package com.gaming.publisher.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration centralisée pour Kafka.
 *
 * Cette classe configure :
 * – Les producteurs Kafka (pour publier des événements)
 * – Les consommateurs Kafka (pour recevoir des événements)
 * – La sérialisation/désérialisation Avro via Schema Registry.
 *
 * PRINCIPE DRY : Toute la configuration Kafka est centralisée ici,
 * évitant la duplication de code dans chaque producer/consumer.
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${kafka.consumer.group-id}")
    private String consumerGroupId;

    /**
     * Configuration de base pour les producteurs Kafka.
     *
     * FONCTIONNEMENT :
     * 1. bootstrap.servers : Adresse du cluster Kafka
     * 2. key.serializer : Sérialise les clés en String
     * 3. value.serializer : Sérialise les valeurs en Avro
     * 4. schema.registry.url : URL du Schema Registry pour validation
     *
     * @return Map de configuration
     */
    @Bean
    @Primary
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();

        // Connexion au cluster Kafka
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Sérialisation des clés et valeurs
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        // Configuration du Schema Registry
        props.put("schema.registry.url", schemaRegistryUrl);

        // Fiabilité : attendre l'acknowledgement de tous les réplicas
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // Retry en cas d'erreur temporaire
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Idempotence : évite les doublons en cas de retry
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return props;
    }

    /**
     * Configuration de base pour les consommateurs Kafka.
     *
     * FONCTIONNEMENT :
     * 1. bootstrap.servers : Adresse du cluster Kafka
     * 2. group.id : Groupe de consommateurs (partage la charge)
     * 3. key.deserializer : Désérialise les clés String
     * 4. value.deserializer : Désérialise les valeurs Avro
     * 5. specific.avro.reader : Utilise les classes générées (pas GenericRecord)
     *
     * @return Map de configuration
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();

        // Connexion au cluster Kafka
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Groupe de consommateurs (permet le load balancing)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        // Désérialisation des clés et valeurs
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());

        // Configuration du Schema Registry
        props.put("schema.registry.url", schemaRegistryUrl);

        // Utiliser les classes Avro spécifiques générées (pas GenericRecord)
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        // Commit automatique des offsets (simplifié pour ce projet)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        // Lire depuis le début si aucun offset n'est trouvé
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }

    /**
     * Bean pour le consumer de crashs.
     * Création explicite pour éviter les dépendances circulaires.
     */
    @Bean
    public Map<String, Object> crashConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    /**
     * Bean pour le consumer de ratings.
     * Création explicite pour éviter les dépendances circulaires.
     */
    @Bean
    public Map<String, Object> ratingConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}

