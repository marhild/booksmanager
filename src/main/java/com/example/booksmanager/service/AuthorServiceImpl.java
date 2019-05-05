package com.example.booksmanager.service;

import com.example.booksmanager.domain.Author;
import com.example.booksmanager.exception.ResourceNotFoundException;
import com.example.booksmanager.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author platoiscoding.com
 */
@Service
public class AuthorServiceImpl implements AuthorService{

    @Autowired
    private  AuthorRepository authorRepository;

    /**
     * @return all authors from database
     */
    @Override
    public Set<Author> getAll() {
        Set<Author> authorSet = new HashSet<>();
        authorRepository.findAll().iterator().forEachRemaining(authorSet::add);
        return authorSet;
    }

    /**
     * finds an author from database by id
     * @param id    author_id
     * @return      author with matching id
     */
    @Override
    public Author findById(Long id) {
        Optional<Author> authorOptional = authorRepository.findById(id);
        if(!authorOptional.isPresent()){
            throw new RuntimeException("Author Not Found!");
        }
        return authorOptional.get();
    }

    /**
     * creates and saves new author into database
     * @param author  entity
     * @return        newest Author from database (this author)
     */
    @Override
    public Author create(Author author) {
        //TODO structure to enforce this function??
        author.setFullName();
        authorRepository.save(author);
        return getLatestEntry();
    }

    /**
     * updates author from database with field values in authordetails
     * @param id    author_id
     * @param author  authordetails
     */
    @Override
    public void update(Long id, Author author) {
        Author currentAuthor = findById(id);
        currentAuthor.setFirstName(author.getFirstName());
        currentAuthor.setLastName(author.getLastName());
        currentAuthor.setFullName();
        currentAuthor.setBio(author.getBio());
        currentAuthor.setUpdatedAt(new Date());
        authorRepository.save(currentAuthor);
    }

    /**
     * deletes author from database
     * @param id    author_id
     */
    @Override
    public void delete(Long id) {
        authorRepository.deleteById(id);
    }

    /**
     * @return newest author in the database
     */
    @Override
    public Author getLatestEntry() {
        Set<Author> authors = getAll();
        if(authors.isEmpty()){
            throw new ResourceNotFoundException("There are no Authors in your DB");
        }else{
            Long latestAuthorId = authorRepository.findTopByOrderByIdDesc();
            return findById(latestAuthorId);
        }
    }

    /**
     * @param author
     * @return false if there exists an author with same name in the database
     */
    @Override
    public boolean authorNameValid(Author author) {
        Set<Author> authorSet = new HashSet<>();
        authorRepository.findByAuthorFullName(author.getFullName())
                        .iterator().forEachRemaining(authorSet::add);
        return !authorSet.isEmpty();
    }

    /**
     * A Page is a sublist of a list of objects
     * @param pageable  Abstract interface for pagination information
     * @return          all authors from databse as Page<> object
     */
    @Override
    public Page<Author> findAll(Pageable pageable){
        return authorRepository.findAll(pageable);
    }

    @Override
    public Page<Author> findAllByBooks(Book book, Pageable pageable){
        return authorRepository.findAllByBooks(book, pageable);
    }
}
