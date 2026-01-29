package com.controller;

import com.gaming.api.models.PublisherModel;
import com.mapper.PublisherMapper;
import com.model.Publisher;
import com.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping
    public ResponseEntity<PublisherModel> createPublisher(@RequestBody PublisherModel publisherModel) {
        Publisher publisher = publisherMapper.fromDTO(publisherModel);
        return ResponseEntity.ok(publisherService.createPublisher(publisher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublisherModel> updatePublisher(@PathVariable String id, @RequestBody PublisherModel publisherModel) {
        Publisher publisher = publisherMapper.fromDTO(publisherModel);
        publisher.setId(id);
        return ResponseEntity.ok(publisherService.updatePublisher(id, publisher));
    }

    //TODO : Verif authentification
}
