package com.example.booksmanager.repository;


import com.example.booksmanager.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface BookRepository extends PagingAndSortingRepository<Book, Long> {
    /**
     * @return newest bookId
     */
    @Query(value = "SELECT MAX(book_id) FROM Book")
    Long findTopByOrderByIdDesc();

    /**
     * @param title     title of a book
     * @param author    author of a book
     * @return          List of articles with the same title and author
     */
    //@Query("SELECT a FROM Book a WHERE a.title=:title and a.author=:author")
    //Set<Book> findByTitleAndAuthor(@Param("title") String title, @Param("author") String author);

    @Query("SELECT a.book_id FROM Book a " +
            "INNER JOIN author_books b " +
            "            ON a.book_id = b.book_id " +
            "        INNER JOIN Author c " +
            "            ON b.author_id = c.author_id " +
            "WHERE a.title=:title and c.author=:author")
    Set<Long> findByTitleAndAuthor(@Param("title") String title, @Param("author") String author);
    //TODO lol das wird nicht funken

    /**
     * @param           pageable
     * @return          a page of entities that fulfill the restrictions
     *                  specified by the Pageable object
     */
    Page<Book> findAll(Pageable pageable);

}
