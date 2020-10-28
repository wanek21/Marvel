package com.marvel.repository;

import com.marvel.models.Character;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CharacterRepository extends MongoRepository<Character,String> {

    public Character findByName(String name);
    public Page<Character> findAll(Pageable pageable);
}
