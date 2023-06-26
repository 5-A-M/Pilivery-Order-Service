package com.fiveam.orderservice.exception.controller;


import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.DataException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import com.fiveam.orderservice.exception.bussiness.BusinessLogicException;
import com.fiveam.orderservice.exception.response.ErrorResponse;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> methodArgumentNotValidException(
            MethodArgumentNotValidException e ){
        ErrorResponse response = ErrorResponse.of(e.getBindingResult());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> constraintViolationException(
            ConstraintViolationException e ){
        ErrorResponse response = ErrorResponse.of(e.getConstraintViolations());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> businessLogicException( BusinessLogicException e ){
        ErrorResponse response = ErrorResponse.of(e.getExceptionCode());
        log.error("비지니스 예외 처리");
        return new ResponseEntity<>(response, HttpStatus.valueOf(e.getExceptionCode().getCode()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> httpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e ){

        ErrorResponse response = ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED);

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> httpMessageNotReadableException(
            HttpMessageNotReadableException e ){

        ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> missingServletRequestParameterException(
            MissingServletRequestParameterException e ){

        ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> incorrectResultSizeDataAccessException(
            IncorrectResultSizeDataAccessException e ){
        ErrorResponse response = ErrorResponse.of(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
        log.error("1: NOT_ACCEPTABLE" + e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
    }
  @ExceptionHandler
    public ResponseEntity<ErrorResponse> HttpClientErrorException(
          HttpClientErrorException e, HttpServletRequest request){
        ErrorResponse response = ErrorResponse.of(HttpStatus.NOT_ACCEPTABLE, e.getResponseBodyAsString());
        log.error("2: NOT_ACCEPTABLE" + e.getMessage());
        log.error(request.getHeader("Authorization"));

        return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
    }


    @ExceptionHandler
    public ResponseEntity<ErrorResponse> dataException(
            DataException e ){

        ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
