package com.repository;

import com.model.Patch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatchRepository extends JpaRepository<Patch, String> {
}
