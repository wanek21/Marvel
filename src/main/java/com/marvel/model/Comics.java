package com.marvel.model;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// класс комикса
public class Comics {

    @Id
    private String id;

    private String title;
    private String content; // со
    private List<Character> characters;

    public Comics() {}
    public Comics(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    @ApiModelProperty(hidden = true)
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    @Override
    public String toString() {
        return "Title: " + this.title +
                "\nContent: " + this.content;
    }

    @ApiModelProperty(hidden = true)
    public void setCharacters(List<Character> characters) {
        this.characters = characters;
    }

    public static final Comparator<Comics> COMPARE_BY_TITLE = (c1, c2) -> {
        int res = String.CASE_INSENSITIVE_ORDER.compare(c1.getTitle(), c2.getTitle());
        if (res == 0) {
            res = c1.getTitle().compareTo(c2.getTitle());
        }
        return res;
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comics comics = (Comics) o;
        return Objects.equals(id, comics.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
