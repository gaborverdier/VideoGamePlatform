package com.service;

import com.gaming.api.dto.PublisherDTO;
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

    public List<PublisherDTO> getAllPublishers() {
        return publisherRepository.findAll().stream()
            .map(PublisherMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Optional<PublisherDTO> getPublisherById(Long id) {
        return publisherRepository.findById(id)
            .map(PublisherMapper::toDTO);
    }

    public PublisherDTO createPublisher(Publisher publisher) {
        Publisher saved = publisherRepository.save(publisher);
        return PublisherMapper.toDTO(saved);
    }

    public PublisherDTO updatePublisher(Long id, Publisher publisherDetails) {
        // Validation métier : le publisher doit exister
        Publisher publisher = publisherRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Publisher introuvable avec l'ID: " + id));
        
        publisher.setName(publisherDetails.getName());
        Publisher updated = publisherRepository.save(publisher);
        return PublisherMapper.toDTO(updated);
    }

    public void deletePublisher(Long id) {
        // Validation métier : le publisher doit exister
        if (!publisherRepository.existsById(id)) {
            throw new IllegalArgumentException("Publisher introuvable avec l'ID: " + id);
        }
        publisherRepository.deleteById(id);
    }
}
