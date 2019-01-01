package com.example.booksmanager.domain;

import com.example.booksmanager.dateAudit.DateAudit;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "categories")
public class Category extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotEmpty
    private String name;

    @ManyToMany(mappedBy = "categories")
    private Set<Book> books;

    public Category() {
        setCreatedAt(new Date());
        setUpdatedAt(new Date());
    }

    //for uncategorized books
    public Category(String name){
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }
}