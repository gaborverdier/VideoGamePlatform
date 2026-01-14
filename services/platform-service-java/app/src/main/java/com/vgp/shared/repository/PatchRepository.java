package com.vgp.shared.repository;

import com.vgp.shared.entity.Patch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatchRepository extends JpaRepository<Patch, Integer> {
}
