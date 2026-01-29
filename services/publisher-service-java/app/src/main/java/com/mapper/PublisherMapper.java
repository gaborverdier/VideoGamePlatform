package com.mapper;

import com.model.Publisher;
import com.gaming.api.models.PublisherModel;
import com.gaming.api.requests.NewPublisherRequest;
import com.gaming.api.requests.PublisherAuth;

import org.springframework.stereotype.Component;


@Component

public class PublisherMapper {
    public PublisherModel toDTO(Publisher publisher) {
        if (publisher == null) throw new IllegalArgumentException("Publisher ne peut pas être null");
        PublisherModel dto = new PublisherModel();
        dto.setId(publisher.getId());
        dto.setName(publisher.getName());
        dto.setEmail(publisher.getEmail());
        dto.setIsCompany(publisher.isCompany());
        return dto;
    }

    public Publisher fromDTO(PublisherModel dto) {
        if (dto == null) throw new IllegalArgumentException("Le DTO ne peut pas être null");
        Publisher publisher = new Publisher();
        publisher.setId(dto.getId());
        publisher.setName(dto.getName());
        return publisher;
    }

    public Publisher fromNewPublisherRequest(NewPublisherRequest request) {
        if (request == null) throw new IllegalArgumentException("La requête ne peut pas être null");
        Publisher publisher = new Publisher();
        publisher.setName(request.getName());
        publisher.setPassword(request.getPassword());
        publisher.setEmail(request.getEmail());
        publisher.setCompany(request.getIsCompany());
        return publisher;
    }

    public Publisher fromPublisherAuth(PublisherAuth auth) {
        if (auth == null) throw new IllegalArgumentException("L'authentification ne peut pas être null");
        Publisher publisher = new Publisher();
        publisher.setEmail(auth.getEmail());
        publisher.setPassword(auth.getPassword());
        return publisher;
    }


}
