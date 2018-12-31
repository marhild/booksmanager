package com.example.booksmanager.repository;

import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Long> {
    /**
     * @return newest categoryId
     */
    @Query(value = "SELECT MAX(id) FROM Category")
    Long findTopByOrderByIdDesc();
    /**
     * @param           pageable
     * @return          a page of entities that fulfill the restrictions
     *                  specified by the Pageable object
     */
    Page<Category> findAll(Pageable pageable);
}
