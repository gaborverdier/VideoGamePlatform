package com.repository;

import com.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, String> {

    Optional<Publisher> findByEmailAndPassword(String email, String password);
    Optional<Publisher> findByEmail(String email);
}
