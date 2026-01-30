package com.mapper;

import org.springframework.stereotype.Component;
import com.model.CrashAggregation;
import com.gaming.api.models.CrashAggregationModel;

@Component
public class CrashAggregationMapper {

    /**
     * Convertit un événement Avro CrashAggregationModel en entité JPA CrashAggregation
     */
    public CrashAggregation fromAvro(CrashAggregationModel avroModel) {
        if (avroModel == null) {
            throw new IllegalArgumentException("L'événement Avro ne peut pas être null");
        }

        return CrashAggregation.builder()
                .id(avroModel.getId().toString())
                .gameId(avroModel.getGameId().toString())
                .crashCount(avroModel.getCrashCount())
                .timestamp(avroModel.getTimestamp())
                .windowStart(avroModel.getWindowStart())
                .windowEnd(avroModel.getWindowEnd())
                .build();
    }

    /**
     * Convertit une entité JPA CrashAggregation en événement Avro CrashAggregationModel
     */
    public CrashAggregationModel toAvro(CrashAggregation entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité CrashAggregation ne peut pas être null");
        }

        return CrashAggregationModel.newBuilder()
                .setId(entity.getId())
                .setGameId(entity.getGameId())
                .setCrashCount(entity.getCrashCount())
                .setTimestamp(entity.getTimestamp())
                .setWindowStart(entity.getWindowStart())
                .setWindowEnd(entity.getWindowEnd())
                .build();
    }
}
