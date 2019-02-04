package com.example.booksmanager.exception;

public class RemoveCategoryException extends RuntimeException{
    public RemoveCategoryException(){super();}

    public RemoveCategoryException(String message) {super(message);}

    public RemoveCategoryException(String message, Throwable cause){
        super(message,cause);
    }
}
