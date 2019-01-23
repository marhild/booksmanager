package com.example.booksmanager.service;

import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import com.example.booksmanager.repository.BookRepository;
import com.example.booksmanager.repository.CategoryRepository;
import com.example.booksmanager.support.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author platoiscoding.com
 */
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookServiceImpl(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * @return all books in database
     */
    @Override
    public Set<Book> getBooks(){
        Set<Book> bookSet = new HashSet<>();
        bookRepository.findAll().iterator().forEachRemaining(bookSet::add);
        return bookSet;
    }

    /**
     * finds a book from database by id
     * @param id    book_id
     * @return      book with matching id
     */
    @Override
    public Book findById(Long id){
        Optional<Book> bookOptional = bookRepository.findById(id);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("Book Not Found!");
        }
        return bookOptional.get();
    }

    /**
     * creates and saves new book into database
     * @param book  entity
     * @return      newest book from database(this book)
     */
    @Override
    public Book create(Book book){
        bookRepository.save(book);
        return getLatestEntry();
    }

    /**
     * updates book from database with fields values in bookdetails
     * @param id    book_id
     * @param book  bookdetails
     */
    @Override
    public void update(Long id, Book book){
        Book currentBook = findById(id);
        currentBook.setTitle(book.getTitle());
        currentBook.setAuthor(book.getAuthor());
        currentBook.setCategories(book.getCategories());
        currentBook.setDescription(book.getDescription());
        currentBook.setYear(book.getYear());
        currentBook.setUpdatedAt(new Date());
        bookRepository.save(currentBook);
    }

    /**
     * deletes book from database
     * @param id    book_id
     */
    @Override
    public void delete(Long id){
        bookRepository.deleteById(id);
    }

    /**
     * @return newest article in the database
     */
    @Override
    public Book getLatestEntry(){
        Set<Book> books = getBooks();
        if(books.isEmpty()){
            return null;
        } else{
            Long latestBookId = bookRepository.findTopByOrderByIdDesc();
            return findById(latestBookId);
        }
    }

    /**
     * Will remove a book from a category nad vice versa
     * @param book          book to remove from category
     * @param category      category to remove from book
     */
    @Override
    public void removeFromCategory(Book book, Category category){
        Set<Category> categoriesOfBook = book.getCategories();
        Set<Book> booksOfCategory = category.getBooks();


        //remove Book from Category
        booksOfCategory.removeIf( b -> (b.getId() == book.getId()));
        category.setBooks(booksOfCategory);
        category.setUpdatedAt(new Date());
        categoryRepository.save(category);

        //remove Category from Book
        categoriesOfBook.removeIf( cat -> (cat.getId() == category.getId()));
        book.setCategories(categoriesOfBook);
        book.setUpdatedAt(new Date());
        bookRepository.save(book);
    }

    /**
     * tests whether there is an book with te same title and author in the database
     * @param book
     * @return true if there is no book with the same author and title in the database
     */

    @Override
    public boolean titleAndAuthorValid(Book book) {
        Set<Book> bookSet = new HashSet<>();
        bookRepository.findByTitleAndAuthor(book.getTitle(),book.getAuthor())
                .iterator().forEachRemaining(bookSet::add);
        if (!bookSet.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * A Page is a sublist of a list of objects
     * @param pageable  Abstract interface for pagination information
     * @return          all books from databse as Page<> object
     */
    @Override
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

}
