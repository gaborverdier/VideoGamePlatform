package org.example.models;

public enum Platform {
    PC("PC"),
    PS5("PS5"),
    XBOX("Xbox"),
    SWITCH("Switch");

    private final String label;

    Platform(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
