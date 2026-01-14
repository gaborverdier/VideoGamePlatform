package com.vgp.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameDto {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("editorId")
    private Integer editorId;
    
    @JsonProperty("editorName")
    private String editorName;
    
    public GameDto() {}
    
    public GameDto(Integer id, String name, Integer editorId, String editorName) {
        this.id = id;
        this.name = name;
        this.editorId = editorId;
        this.editorName = editorName;
    }
    
    // Getters and setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getEditorId() {
        return editorId;
    }
    
    public void setEditorId(Integer editorId) {
        this.editorId = editorId;
    }
    
    public String getEditorName() {
        return editorName;
    }
    
    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }
}
