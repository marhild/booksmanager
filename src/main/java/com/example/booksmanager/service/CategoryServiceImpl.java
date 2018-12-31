package com.example.booksmanager.service;


import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import com.example.booksmanager.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Set<Category> getCategories(){
        Set<Category> categorySet = new HashSet<>();
        categoryRepository.findAll().iterator().forEachRemaining(categorySet::add);
        return categorySet;
    }

    @Override
    public Category findById(Long id){
        Optional<Category> categoryOptional = categoryRepository.findById(id);

        if (!categoryOptional.isPresent()) {
            throw new RuntimeException("Category Not Found!");
        }
        return categoryOptional.get();
    }

    @Override
    public void delete(Long id){
        categoryRepository.deleteById(id);
    }

    @Override
    public Category create(Category category){
        categoryRepository.save(category);
        return getLatestEntry();
    }

    @Override
    public void update(Long id, Category category){
        Category currentCat = findById(id);
        currentCat.setName(category.getName());

        categoryRepository.save(currentCat);
    }

    //Pagination
    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category getLatestEntry(){
        Set<Category> categories = getCategories();
        if(categories.isEmpty()){
            return null;
        }
        else{
            Long latestBookId = categoryRepository.findTopByOrderByIdDesc();
            return findById(latestBookId);
        }
    }

    /*
    @Override
    public Set<Category> getCategoriesIdByBookId(Long BookId){
        Set<Category> bookCategories = new HashSet<>();
        Set<Long> CategoryIds =  categoryRepository.getCategoriesIdByBookId(BookId);

        for (Long id : CategoryIds) {
            System.out.println("Category_id" + id);
            bookCategories.add(findById(id));
        }

        return bookCategories;
    }*/

}
