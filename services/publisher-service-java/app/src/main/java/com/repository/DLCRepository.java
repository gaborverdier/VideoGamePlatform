package com.repository;

import com.model.DLC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DLCRepository extends JpaRepository<DLC, String> {
}
