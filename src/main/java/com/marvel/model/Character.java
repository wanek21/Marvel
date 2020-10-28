package com.marvel.model;

import org.springframework.data.annotation.Id;

import java.util.Comparator;
import java.util.Objects;

// класс персонажа
public class Character {

    @Id
    private String id;

    private String name;
    private String description;
    private String imageUri;

    public Character() {}

    public Character(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public String toString() {
        return "Name: " + this.name +
                "\nDescription: " + this.description;
    }

    public static final Comparator<Character> COMPARE_BY_NAME = (c1, c2) -> {
        int res = String.CASE_INSENSITIVE_ORDER.compare(c1.getName(), c2.getName());
        if (res == 0) {
            res = c1.getName().compareTo(c2.getName());
        }
        return res;
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Character character = (Character) o;
        return Objects.equals(id, character.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
