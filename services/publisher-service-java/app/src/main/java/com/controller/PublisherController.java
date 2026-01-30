package com.controller;

import com.gaming.api.models.PublisherModel;
import com.gaming.api.requests.NewPublisherRequest;
import com.gaming.api.requests.PublisherAuth;
import com.mapper.PublisherMapper;
import com.model.Publisher;
import com.service.PublisherService;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/publishers")
public class PublisherController {
    @Autowired
    private PublisherService publisherService;
    @Autowired
    private PublisherMapper publisherMapper;


    @GetMapping
    public ResponseEntity<List<PublisherModel>> getAllPublishers() {
        return ResponseEntity.ok(publisherService.getAllPublishers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherModel> getPublisherById(@PathVariable String id) {
        return publisherService.getPublisherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/auth")
    public ResponseEntity<PublisherModel> authenticatePublisher(@RequestBody PublisherAuth auth) {
        Publisher pub = publisherMapper.fromPublisherAuth(auth);
        return publisherService.authenticatePublisher(pub)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping
    public ResponseEntity<?> createPublisher(@RequestBody NewPublisherRequest publisherModel) {
        Publisher publisher = publisherMapper.fromNewPublisherRequest(publisherModel);
        if (publisher == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<PublisherModel> newPublisher = publisherService.createPublisher(publisher);
        if (!newPublisher.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Un publisher existe déjà avec ce mail");
        } else {
            return ResponseEntity.ok(newPublisher.get());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublisherModel> updatePublisher(@PathVariable String id, @RequestBody PublisherModel publisherModel) {
        Publisher publisher = publisherMapper.fromDTO(publisherModel);
        publisher.setId(id);
        return ResponseEntity.ok(publisherService.updatePublisher(id, publisher));
    }
}
