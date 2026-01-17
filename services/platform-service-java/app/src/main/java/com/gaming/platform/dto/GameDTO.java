package com.gaming.platform.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameDTO {
    private String gameId;
    private String title;
    private String publisher;
    private String platform;
    private String genre;
    private Integer releaseYear;
    private BigDecimal price;
    private String version;
    private Boolean available;
    private String description;
}