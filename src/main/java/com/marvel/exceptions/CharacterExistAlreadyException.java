package com.marvel.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Character with that name is already engaged")
public class CharacterExistAlreadyException extends RuntimeException {
}
