package com.marvel.rest;

import com.marvel.exceptions.CharacterNotFoundException;
import com.marvel.exceptions.ComicsNotFoundException;
import com.marvel.model.Character;
import com.marvel.model.Comics;
import com.marvel.repository.CharacterRepository;
import com.marvel.repository.ComicsRepository;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/public/comics")
public class ComicsRestController extends BaseRestController{

    @Autowired
    ComicsRepository comicsRepository;
    @Autowired
    CharacterRepository characterRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @ApiOperation(value = "Вывести все комиксы")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title", value = "Фильтрация по названию (поиск подстроки)", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "characters", value = "Поиск комиксов с данными персонажами. Вводить через запятую с большой буквы: Spider Man,Thanos", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = "Сортировка по названию, по алфавиту", allowableValues = "asc,desc", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "Номер страницы", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "Кол-во комиксов на странице", required = false, dataType = "int", paramType = "query")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Internal server error") })
    @GetMapping("")
    public Page<Comics> getAllComics(Pageable pageable,
                              @RequestParam(value = "title",required = false) String title,
                              @RequestParam(value = "characters",required = false) List<String> characters,
                              @RequestParam(value = "sort", required = false) String sort) {

        List<Comics> comics = comicsRepository.findAll();

        if (title != null && !title.equals("")) { // если есть фильтр по названию, то фильтруем
            Query query = new Query();
            query.addCriteria(Criteria.where("title").regex("(?i:.*"+title+".*)"));

            comics.clear();
            comics = mongoTemplate.find(query,Comics.class);
        }
        if(characters != null && !characters.isEmpty()) { // если есть фильтр по имени персонажа, то фильтруем
            ArrayList<Comics> tempComics = new ArrayList<>();
            ArrayList<Character> characterList = new ArrayList<>();
            for (String name : characters) { // находим персонажей в бд по имени и заносим их в список
                Query query = new Query();
                query.addCriteria(Criteria.where("name").is(name));
                characterList.addAll(mongoTemplate.find(query,Character.class));
            }
            for (Comics c : comics) { // ищем комиксы, в которых есть эти персонажи и заноим их в tempComics
                for(Character chr: c.getCharacters()) {
                    if(characterList.contains(chr)) tempComics.add(c);
                }
            }
            comics = tempComics;
            comics = comics.stream().distinct().collect(Collectors.toList()); // удаляем одинаковые комиксы
        }

        if(sort != null && !sort.equals("")) { // если есть сортировка, то сортируем
            if(sort.equalsIgnoreCase("asc")) {
                comics.sort(Comics.COMPARE_BY_TITLE);
            } else if(sort.equalsIgnoreCase("desc")) comics.sort(Collections.reverseOrder(Comics.COMPARE_BY_TITLE));

        }
        return listToPage(comics,pageable);
    }


    @ApiOperation(value = "Вывести информацию о комиксе")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id комикса", required = true, dataType = "string", paramType = "path")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Comics not found"),
            @ApiResponse(code = 400, message = "Invalid input data"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @GetMapping("{id}")
    public ResponseEntity<Comics> getComics(@PathVariable("id") String id) {
        Comics comics = null;
        if(comicsRepository.findById(id).isPresent()) comics = comicsRepository.findById(id).get();
        if(comics == null) throw new ComicsNotFoundException();
        return new ResponseEntity<>(comics, HttpStatus.OK);
    }

    @ApiOperation(value = "Вывести всех персонажей в данном комиксе")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id комикса", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "sort", value = "Сортировка по имени, по алфавиту", allowableValues = "asc,desc", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "Номер страницы", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "Кол-во персонажей на странице", required = false, dataType = "int", paramType = "query")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Comics not found"),
            @ApiResponse(code = 400, message = "Invalid input data"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @GetMapping("{id}/characters")
    public Page<Comics> getCharacters(Pageable pageable,
                                      @PathVariable("id") String id,
                                      @RequestParam(value = "sort",required = false) String sort) {
        Comics comics = null;
        if(comicsRepository.findById(id).isPresent()) comics = comicsRepository.findById(id).get();
        if(comics == null) throw new ComicsNotFoundException();

        List<Character> characters = comics.getCharacters();
        if(sort != null && !sort.equals("")) { // если есть сортировка, то сортируем
            if(sort.equalsIgnoreCase("asc")) {
                characters.sort(Character.COMPARE_BY_NAME);
            } else if(sort.equalsIgnoreCase("desc")) characters.sort(Collections.reverseOrder(Character.COMPARE_BY_NAME));

        }
        return listToPage(characters,pageable);
    }

    @ApiOperation(value = "Добавить комикс")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid input data"),
            @ApiResponse(code = 404, message = "Character not found"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "characters", value = "Добавить персонажей. С большой буквы, через запятую, пример: Thanos,Spider Man", required = false, dataType = "string", paramType = "query")
    })
    // добавить комикс
    @PostMapping("")
    public ResponseEntity<Comics> addComics(@RequestBody Comics comics,
                                            @RequestParam(value = "characters", required = false) List<String> characters) throws URISyntaxException {
        List<Character> resultCharacters = new ArrayList<>();

        if(characters != null && !characters.isEmpty()) {
            // получем список персонажей из бд
            for (String chr : characters) {
                Character resultCharacter = characterRepository.findByName(chr);
                if(resultCharacter == null) {
                    throw new CharacterNotFoundException();
                }
                else resultCharacters.add(resultCharacter);
            }
        }

        comics.setCharacters(resultCharacters);
        comicsRepository.insert(comics);
        return getRedirect("/v1/public/comics");
    }
}
