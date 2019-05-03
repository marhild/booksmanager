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
    @Query(value = "SELECT MAX(id) FROM Book")
    Long findTopByOrderByIdDesc();

    /**
     * @param title     title of a book
     * @return          List of articles with the same title
     * */
    @Query("SELECT a FROM Book a WHERE a.title=:title")
    Iterable<Book> findByTitle(@Param("title") String title);

    /**
     * @param           pageable
     * @return          a page of entities that fulfill the restrictions
     *                  specified by the Pageable object
     */
    Page<Book> findAll(Pageable pageable);

}
