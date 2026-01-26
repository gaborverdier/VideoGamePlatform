package com.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @OneToMany(mappedBy = "publisher")
    private List<Game> games;
}
