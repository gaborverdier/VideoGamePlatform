package com.vgp.patch;

import com.vgp.patch.Patch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PatchRepository extends JpaRepository<Patch, UUID> {
}