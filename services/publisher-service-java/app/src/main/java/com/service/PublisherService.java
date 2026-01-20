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
        return publisherRepository.findById(id).map(publisher -> {
            publisher.setName(publisherDetails.getName());
            return publisherRepository.save(publisher);
        }).orElse(null);
    }

    public void deletePublisher(Long id) {
        publisherRepository.deleteById(id);
    }
}
