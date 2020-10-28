package com.marvel.repository;

import com.marvel.model.Comics;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComicsRepository extends MongoRepository<Comics, String> {

}
