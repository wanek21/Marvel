package com.marvel.rest;

import com.marvel.exceptions.CharacterExistAlreadyException;
import com.marvel.exceptions.CharacterNotFoundException;
import com.marvel.exceptions.FileIsEmptyException;
import com.marvel.model.Character;
import com.marvel.model.Comics;
import com.marvel.repository.CharacterRepository;
import com.marvel.repository.ComicsRepository;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1/public/characters")
public class CharacterRestController extends BaseRestController{

    @Autowired
    private CharacterRepository characterRepository;
    @Autowired
    private ComicsRepository comicsRepository;
    @Autowired
    private MongoTemplate mongoTemplate;


    @ApiOperation(value = "Вывести данные о всех персонажах", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "Фильтрация по имени (поиск подстроки)", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = "Сортировка по имени, по алфавиту", allowableValues = "asc,desc", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "Номер страницы", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "Кол-во персонажей на странице", required = false, dataType = "int", paramType = "query")
    })
    @GetMapping("")
    public Page<Character> getAllCharacters(Pageable pageable,
                                 @RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "sort", required = false) String sort) {
        Query query = new Query();
        if(name != null && !name.equals("")) query.addCriteria(Criteria.where("name").regex("(?i:.*"+name+".*)"));

        List<Character> characters = mongoTemplate.find(query,Character.class);
        if(sort != null && !sort.equals("")) { // если есть сортировка, сортируем
            if(sort.equalsIgnoreCase("asc")) {
                characters.sort(Character.COMPARE_BY_NAME);
            } else if(sort.equalsIgnoreCase("desc")) characters.sort(Collections.reverseOrder(Character.COMPARE_BY_NAME));

        }
        return listToPage(characters,pageable);
    }

    @ApiOperation(value = "Вывести данные о персонаже", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id персонажа", required = true, dataType = "string", paramType = "path")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Character not found") })
    @GetMapping("/{id}")
    public ResponseEntity<Character> getCharacter(@PathVariable("id") String id) {
        Character character = null;
        if(characterRepository.findById(id).isPresent()) {
            character = characterRepository.findById(id).get();
        }
        if (character == null) throw new CharacterNotFoundException();
        return new ResponseEntity<>(character, HttpStatus.OK);
    }

    @ApiOperation(value = "Вывести комиксы персонажа с данным id", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title", value = "Фильтрация по названию (поиск подстроки)", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = "Сортировка по названию, по алфавиту", allowableValues = "asc,desc", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "Номер страницы", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "Кол-во комиксов на странице", required = false, dataType = "int", paramType = "query")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid input json"),
            @ApiResponse(code = 404, message = "Character not found") })
    @GetMapping("/{id}/comics")
    public Page<Character> getComics(Pageable pageable,
                                     @PathVariable("id") String characterId,
                                     @RequestParam(value = "title", required = false) String title,
                                     @RequestParam(value = "sort", required = false) String sort) {
        Character currentCharacter = null;
        if(characterRepository.findById(characterId).isPresent()) {
            currentCharacter = characterRepository.findById(characterId).get();
        }
        if (currentCharacter == null) throw new CharacterNotFoundException();

        List<Comics> resultComics = new ArrayList<>();
        List<Comics> allComics = comicsRepository.findAll();
        for (Comics comics : allComics) { // ищем комиксы, в которых есть данный персонаж
            if (comics.getCharacters() != null) {
                for (Character ch : comics.getCharacters()) {
                    if (currentCharacter.equals(ch)) {
                        resultComics.add(comics);
                    }
                }
            }
        }

        if(title != null && !title.equals("")) { // если есть фильтр на название комикса, то фильтруем
            ArrayList<Comics> tempComics = new ArrayList<>();
            resultComics.forEach(c -> {
                if(StringUtils.containsIgnoreCase(c.getTitle(), title)) {
                    tempComics.add(c);
                }
            });
            resultComics.clear();
            resultComics = tempComics;
        }
        if(sort != null && !sort.equals("")) { // если есть сортировка, сортируем
            if(sort.equalsIgnoreCase("asc")) {
                resultComics.sort(Comics.COMPARE_BY_TITLE);
            } else if(sort.equalsIgnoreCase("desc")) resultComics.sort(Collections.reverseOrder(Comics.COMPARE_BY_TITLE));

        }
        return listToPage(resultComics,pageable);
    }

    @ApiOperation(value = "Добавить персонажа", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "Изображение персонажа", dataType = "__file", required = true, paramType = "form"),
            @ApiImplicitParam(name = "name", value = "Имя персонажа от 2 до 16 символов", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "description", value = "Краткое описание персонажа от 4 до 512 символов", required = true, dataType = "string", paramType = "query")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid input data"),
            @ApiResponse(code = 500, message = "Internal server error") })
    // добавить героя
    @PostMapping("")
    public ResponseEntity<Character> addCharacter(@RequestParam(value = "name") String name,
                                                  @RequestParam(value = "description") String desc,
                                                  @RequestParam(value = "file", required = false) MultipartFile imageFile) {

        Character character = new Character(name, desc);
        if(characterIsValid(character)) {
            if (imageFile != null && !imageFile.isEmpty()) {
                if(characterRepository.findByName(name) != null) throw new CharacterExistAlreadyException(); // проверка на занятость имени
                Character resultCharacter = characterRepository.insert(character);
                try {
                    byte[] bytes = imageFile.getBytes();
                    BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(new File(imageUploadPath + "/" + resultCharacter.getId() + ".jpg")));
                    stream.write(bytes);
                    stream.close();

                    resultCharacter.setImageUri("/images/" + resultCharacter.getId() + ".jpg");
                    characterRepository.save(resultCharacter);

                    return getRedirect("/v1/public/characters");
                } catch (Exception e) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                throw new FileIsEmptyException();
            }
        } else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    private boolean characterIsValid(Character character) {
        if(character.getName().length() < 2 || character.getName().length() > 16) return false;
        if(!character.getName().matches("^\\pL+[\\pL\\pZ\\pP]{0,}")) return false;

        if(character.getDescription().length() < 4 || character.getDescription().length() > 512) return false;

        return true;
    }
}
