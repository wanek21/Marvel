package com.marvel.controllers;

import com.marvel.exceptions.PageNotFoundException;
import com.marvel.models.Character;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

abstract class BaseRestController<T> {

    @Value("${images.upload.path}")
    String imageUploadPath;
    @Value("${server.address}")
    String serverAddres;
    @Value("${server.port}")
    String serverPort;

    /* Метод конвертирует список с POJO объектами в Page список.
       Возвращает Page<T> БЕЗ СОРТИРОВКИ, сортировать спиосок объектов нужно заранее
    */
    public Page<T> listToPage(List<T> model, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), model.size());
        if(start > end) throw new PageNotFoundException();
        return new PageImpl<T>(model.subList(start, end), pageable, model.size());
    }

    // метод перенаправляет пользователя на другую страницу
    public ResponseEntity<Character> getRedirect(String uri) throws URISyntaxException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(new URI("http://" + serverAddres+":"+serverPort + uri));
        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }
}
