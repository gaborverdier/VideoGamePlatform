package com.mapper;

import com.model.Publisher;
import com.gaming.api.dto.PublisherDTO;

public class PublisherMapper {
    public static PublisherDTO toDTO(Publisher publisher) {
        if (publisher == null) throw new IllegalArgumentException("Publisher ne peut pas être null");
        PublisherDTO dto = new PublisherDTO();
        dto.setId(publisher.getId());
        dto.setName(publisher.getName());
        return dto;
    }

    public static Publisher fromDTO(PublisherDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        Publisher publisher = new Publisher();
        publisher.setId(dto.getId());
        publisher.setName(dto.getName());
        return publisher;
    }
}
