package com.example.booksmanager.service;

import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface CategoryService {

    Set<Category> getCategories();

    Category findById(Long id);

    void delete(Long id);

    Category create(Category category);

    void update(Long id, Category category);

    Page<Category> findAll(Pageable pageable);

    Category getLatestEntry();

    boolean nameIsValid(Category category);

}
