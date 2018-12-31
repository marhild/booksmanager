package com.example.booksmanager.repository;


import com.example.booksmanager.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BookRepository extends PagingAndSortingRepository<Book, Long> {
    /**
     * @return newest bookId
     */
    @Query(value = "SELECT MAX(id) FROM Book")
    Long findTopByOrderByIdDesc();

    /**
     * @param title     title of an book
     * @param author    author of an book
     * @return          List of articles with the same title and author
     */
    //@Query("SELECT a FROM Article a WHERE a.title=:title and a.author=:author")
    //Set<Book> findByTitleAndAuthor(@Param("title") String title, @Param("author") String author);

    /**
     * @param           pageable
     * @return          a page of entities that fulfill the restrictions
     *                  specified by the Pageable object
     */
    Page<Book> findAll(Pageable pageable);
}
