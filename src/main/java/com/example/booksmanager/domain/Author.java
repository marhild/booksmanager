package com.example.booksmanager.domain;

import com.example.booksmanager.dateAudit.DateAudit;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
public class Author extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Long id;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String fullName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "author_books",
            joinColumns = { @JoinColumn(name = "author_id") },
            inverseJoinColumns = { @JoinColumn(name = "book_id") })
    private Set<Book> books = new HashSet<>();

    public Author() {
        setCreatedAt(new Date());
        setUpdatedAt(new Date());
    }

    //TODO add constructor &  getter/setter for books

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFullName(){
        this.fullName =  this.firstName + ' ' + this.lastName;
    }

    public String getFullName(){
        return this.firstName + ' ' + this.lastName;
    }
}
