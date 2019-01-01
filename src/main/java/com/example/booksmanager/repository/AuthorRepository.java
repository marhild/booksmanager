package com.example.booksmanager.repository;

import com.example.booksmanager.domain.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

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

}
