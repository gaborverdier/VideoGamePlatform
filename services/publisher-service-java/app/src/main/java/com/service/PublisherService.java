package com.service;

import com.gaming.api.models.PublisherModel;
import com.mapper.PublisherMapper;
import com.model.Publisher;
import com.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PublisherService {
    @Autowired
    private PublisherRepository publisherRepository;
    @Autowired
    private PublisherMapper publisherMapper;

    public List<PublisherModel> getAllPublishers() {
        return publisherRepository.findAll().stream()
            .map(publisherMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Optional<PublisherModel> getPublisherById(String id) {
        return publisherRepository.findById(id)
            .map(publisherMapper::toDTO);
    }

    public PublisherModel createPublisher(Publisher publisher) {
        Publisher saved = publisherRepository.save(publisher);
        return publisherMapper.toDTO(saved);
    }

    public PublisherModel updatePublisher(String id, Publisher publisherDetails) {
        // Validation métier : le publisher doit exister
        Publisher publisher = publisherRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Publisher introuvable avec l'ID: " + id));
        
        publisher.setName(publisherDetails.getName());
        Publisher updated = publisherRepository.save(publisher);
        return publisherMapper.toDTO(updated);
    }

    public void deletePublisher(String id) {
        // Validation métier : le publisher doit exister
        if (!publisherRepository.existsById(id)) {
            throw new IllegalArgumentException("Publisher introuvable avec l'ID: " + id);
        }
        publisherRepository.deleteById(id);
    }
}
