package com.vgp.shared.repository;

import com.vgp.shared.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Integer> {
    
    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Game> searchByTitle(@Param("title") String title);
    
    @Query("SELECT g FROM Game g WHERE g.editor.id = :editorId")
    List<Game> findByEditorId(@Param("editorId") Integer editorId);
}
