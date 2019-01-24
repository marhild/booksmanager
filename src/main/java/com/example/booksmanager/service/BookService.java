package com.example.booksmanager.service;


import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author:platoiscoding.com
 */
@Service
public interface BookService {

    /**
     * @return all books from database
     */
    Set<Book> getBooks();

    /**
     * finds a book from database by id
     * @param id    book_id
     * @return      book with matching id
     */
    Book findById(Long id);

    /**
     * creates and saves new book into database
     * @param book  entity
     * @return      newest book from database(this book)
     */
    Book create(Book book);

    /**
     * updates book from database with fields values in bookdetails
     * @param id    book_id
     * @param book  bookdetails
     */
    void update(Long id, Book book);

    /**
     * deletes book from database
     * @param id    book_id
     */
    void delete(Long id);

    /**
     * A Page is a sublist of a list of objects
     * @param pageable  Abstract interface for pagination information
     * @return          all books from databse as Page<> object
     */
    Page<Book> findAll(Pageable pageable);

    /**
     * @return newest entry from database
     */
    Book getLatestEntry();

    /**
     * Will remove a book from a category nad vice versa
     * @param book          book to remove from category
     * @param category      category to remove from book
     */
    void removeFromCategory(Book book, Category category);

    /**
     * tests whether there is a book with te same title and author in the database
     * @param book
     * @return true if there is no book with the same author and title in the database
     */
    boolean titleAndAuthorValid(Book book);
}
