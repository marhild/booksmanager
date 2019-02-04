package com.example.booksmanager.controller;

import com.example.booksmanager.exception.RemoveCategoryException;
import com.example.booksmanager.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ControllerExceptionHandler {

    /**
     * RemoveCategoryException
     * @param exception
     * @return 400error view
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({RemoveCategoryException.class})
    public ModelAndView handleException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("BadRequest");
        modelAndView.addObject("exception", exception);

        return modelAndView;
    }



    /**
     * Resource Not Found
     * @param exception
     * @return 404error view
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(Exception exception){

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("404error");
        modelAndView.addObject("exception", exception);

        return modelAndView;
    }

}