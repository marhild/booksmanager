package com.example.booksmanager.service;


import com.example.booksmanager.domain.Book;
import com.example.booksmanager.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Set<Book> getBooks(){
        Set<Book> bookSet = new HashSet<>();
        bookRepository.findAll().iterator().forEachRemaining(bookSet::add);
        return bookSet;
    }

    @Override
    public Book findById(Long id){
        Optional<Book> bookOptional = bookRepository.findById(id);

        if (!bookOptional.isPresent()) {
            throw new RuntimeException("Book Not Found!");
        }

        return bookOptional.get();

    }

    @Override
    public void update(Long id, Book book){
        Book currentBook = findById(id);
        currentBook.setTitle(book.getTitle());
        currentBook.setAuthor(book.getAuthor());
        //currentBook.setCategories(book.getCategories());
        currentBook.setDescription(book.getDescription());
        //currentBook.setDateField(book.getDateField());

        bookRepository.save(currentBook);
    }

    @Override
    public void delete(Long id){
        bookRepository.deleteById(id);
    }

    @Override
    public Book create(Book book){
        bookRepository.save(book);
        return getLatestEntry();
    }

    /**
     * @return newest article in the databse
     */
    @Override
    public Book getLatestEntry(){
        Set<Book> books = getBooks();
        if(books.isEmpty()){
            return null;
        }
        else{
            Long latestBookId = bookRepository.findTopByOrderByIdDesc();
            return findById(latestBookId);
        }
    }
    //TODO with optional?

    /**
     * tests whether there is an book with te same title and author in the database
     * @param book
     * @return true if there is no book with the same author and title in the database
     */
    /*
    @Override
    public boolean titleAndAuthorValid(Book book) {
        Set<Book> articleSet = new HashSet<>();
        bookRepository.findByTitleAndAuthor(book.getTitle(),book.getAuthor())
                .iterator().forEachRemaining(articleSet::add);
        if (!articleSet.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }*/

    //Pagination
    @Override
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

}
