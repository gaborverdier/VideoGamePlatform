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

    private String password;
    private String email;
    
    @Column(name = "is_company")
    private boolean isCompany;


    @OneToMany(mappedBy = "publisher")
    private List<Game> games;
}
