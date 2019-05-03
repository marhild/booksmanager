package com.example.booksmanager.repository;

import com.example.booksmanager.domain.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.awt.print.Book;
import java.util.Set;

public interface AuthorRepository extends PagingAndSortingRepository<Author, Long> {
    /**
     * @return newest authorId
     */
    @Query(value = "SELECT MAX(id) FROM Author")
    Long findTopByOrderByIdDesc();

    /**
     * @param           pageable
     * @return          a page of entities that fulfill the restrictions
     *                  specified by the Pageable object
     */
    Page<Author> findAll(Pageable pageable);

    /**
     * @return author by fullname
     */
    @Query(value = "SELECT fn FROM Author fn WHERE fn.fullName=:fullName")
    Set<Author> findByAuthorFullName(@Param("fullName") String fullName);

}
