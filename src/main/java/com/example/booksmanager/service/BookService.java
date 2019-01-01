package com.example.booksmanager.service;


import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface BookService {

    Set<Book> getBooks();

    Book findById(Long id);

    void update(Long id, Book book);

    void delete(Long id);

    Book create(Book book);

    Page<Book> findAll(Pageable pageable);

    /**
     * @return newest article
     */
    Book getLatestEntry();

    void removeFromCategory(Book book, Category category);

    /**
     * tests whether there is a book with te same title and author in the database
     * @param book
     * @return true if there is no book with the same author and title in the database
     */
    //boolean titleAndAuthorValid(Book book);
}
