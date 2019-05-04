package com.example.booksmanager.repository;

import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

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

    /**
     * for validation whether a category with same name already exists
     * @param categoryName
     * @return List of categories with name = categoryName
     */
    @Query(value = "SELECT u.name FROM Category u WHERE u.name = :categoryName")
    Set<Category> findCategoryByName(@Param("categoryName") String categoryName);

    Page<Category> findAllByBooks(Book book, Pageable pageable);
}
