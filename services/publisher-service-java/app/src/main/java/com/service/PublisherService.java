package com.service;

import com.model.Publisher;
import com.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PublisherService {
    @Autowired
    private PublisherRepository publisherRepository;

    public List<Publisher> getAllPublishers() {
        return publisherRepository.findAll();
    }

    public Optional<Publisher> getPublisherById(Long id) {
        return publisherRepository.findById(id);
    }

    public Publisher createPublisher(Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    public Publisher updatePublisher(Long id, Publisher publisherDetails) {
        // Validation métier : le publisher doit exister
        Publisher publisher = publisherRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Publisher introuvable avec l'ID: " + id));
        
        publisher.setName(publisherDetails.getName());
        return publisherRepository.save(publisher);
    }

    public void deletePublisher(Long id) {
        // Validation métier : le publisher doit exister
        if (!publisherRepository.existsById(id)) {
            throw new IllegalArgumentException("Publisher introuvable avec l'ID: " + id);
        }
        publisherRepository.deleteById(id);
    }
}
