package com.example.booksmanager.service;

import com.example.booksmanager.domain.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author:platoiscoding.com
 */
@Service
public interface AuthorService {
    /**
     * @return all authors from database
     */
    Set<Author> getAuthors();

    /**
     * finds a book from database by id
     * @param id    author_id
     * @return      author with matching id
     */
    Author findById(Long id);

    /**
     * creates and saves new book into database
     * @param author  entity
     * @return      newest book from database(this book)
     */
    Author create(Author author);

    /**
     * updates book from database with fields values in authordetails
     * @param id    author_id
     * @param author  authordetails
     */
    void update(Long id, Author author);

    /**
     * deletes author from database
     * @param id    author_id
     */
    void delete(Long id);

    /**
     * A Page is a sublist of a list of objects
     * @param pageable  Abstract interface for pagination information
     * @return          all authors from databse as Page<> object
     */
    Page<Author> findAll(Pageable pageable);

    /**
     * @return newest entry from database
     */
    Author getLatestEntry();


    /**
     * for simplicity we ignore the possibility of multiple authors with the same name
     *
     * tests whether there is already an author with te same name in the database
     * @param author
     * @return true if there is no book with the same author and title in the database
     */
    boolean newAuthorValid(Author author);
}
