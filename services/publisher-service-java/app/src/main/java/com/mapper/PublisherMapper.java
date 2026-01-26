package com.mapper;

import com.model.Publisher;
import com.gaming.api.models.PublisherModel;

import org.springframework.stereotype.Component;

@Component

public class PublisherMapper {
    public PublisherModel toDTO(Publisher publisher) {
        if (publisher == null) throw new IllegalArgumentException("Publisher ne peut pas être null");
        PublisherModel dto = new PublisherModel();
        dto.setId(publisher.getId());
        dto.setName(publisher.getName());
        return dto;
    }

    public Publisher fromDTO(PublisherModel dto) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        Publisher publisher = new Publisher();
        publisher.setId(dto.getId());
        publisher.setName(dto.getName());
        return publisher;
    }
}
