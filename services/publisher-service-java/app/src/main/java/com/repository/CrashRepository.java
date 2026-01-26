package com.repository;

import com.model.Crash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrashRepository extends JpaRepository<Crash, String> {
}
