package com.example.booksmanager.controller;


import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import com.example.booksmanager.service.BookService;
import com.example.booksmanager.service.CategoryService;
import com.example.booksmanager.support.Message;
import com.example.booksmanager.support.PagerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author platoiscoding.com
 */
@Controller
public class BookController {

    //view templates
    protected static final String BOOK_VIEW = "books/showBook";                    //view template for single book
    protected static final String BOOK_ADD_FORM_VIEW = "books/newBook";            //form for new book
    protected static final String BOOK_EDIT_FORM_VIEW = "books/editBook";          //form for editing a book
    protected static final String BOOK_LIST_VIEW = "books/allBooks";               //list view of books with pagination

    //pagination
    private static final int BUTTONS_TO_SHOW = 3;
    private static final int INITIAL_PAGE = 0;
    private static final int INITIAL_PAGE_SIZE = 5;
    private static final int[] PAGE_SIZES = { 5, 10};

    @Autowired
    private BookService bookService;
    @Autowired
    private CategoryService categoryService;

    /**
     * GET book by id
     * After redirect from book/create: model containt attribute "message"(success)
     * @param id        book_id
     * @param model     attributeValues
     * @return          view template for single book
     */
    @RequestMapping( path = "/book/{id}")
    public String showSingleBook(@PathVariable("id") long id, Model model) {
        if(!model.containsAttribute("message")){
            Message message = new Message();
            model.addAttribute("message", message);
        }
        model.addAttribute("book", bookService.findById(id));
        //TODO categories und author müssen links bieten, dazu custom queries
        return BOOK_VIEW;
    }

    /**
     * GET all books from database
     * @param pageSize      number of books per page
     * @param page          subset of all books
     * @return              list view of books
     */
    @RequestMapping({"/books", "/"})
    public ModelAndView showAllBooksWithPagination(@RequestParam("pageSize") Optional<Integer> pageSize,
                                                   @RequestParam("page") Optional<Integer> page) {
        ModelAndView modelAndView = new ModelAndView(BOOK_LIST_VIEW);
        Set<Book> books = bookService.getBooks();
        Message message = new Message();
        if(books.isEmpty()){
            message.setInfo("There are no books in the database.");
        }
        // If pageSize == null, return initial page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        /*
            If page == null || page < 0 (to prevent exception), return initial size
            Else, return value of param. decreased by 1
        */
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;

        Page<Book> booksList = bookService.findAll(PageRequest.of(evalPage, evalPageSize));
        PagerModel pager = new PagerModel(booksList.getTotalPages(),booksList.getNumber(),BUTTONS_TO_SHOW);

        modelAndView.addObject("booksList",booksList);
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        modelAndView.addObject("pager", pager);
        modelAndView.addObject("message", message);

        return modelAndView;
    }

    /**
     * FORM for NEW article
     * in case of redirection model will contain book
     * @param model     attributesValues
     * @return          BOOK_ADD_FORM_VIEW
     */
    @RequestMapping(path = "/book/new")
    public String newBookForm(Model model) {

        Message message = new Message();
        if (!model.containsAttribute("book")) {
            model.addAttribute("book", new Book());
            //TODO categories bei redirect nicht markiert
        }
        else{
            message.setError("Please correct the field errors.");
        }
        Set<Category> allCategories = categoryService.getCategories();
        //TODO multSelect muss eingearbeitet werden
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("message", message);

        return BOOK_ADD_FORM_VIEW;
    }

    /**
     * CREATE NEW book checks...
     *          (1)field values for errors
     *          (2)whether database already contains a book with the same name and author as field values
     * After the redirect: flash attributes pass attributes to the model
     * @param book          entity
     * @param result        result of validation of field values from BOOK_ADD_FORM_VIEW
     * @param model         attributeValues
     * @param attr          stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/book/new'
     *          else:      redirect: '/book/{bookId}'
     */
    @RequestMapping(path = "/book/create", method = RequestMethod.POST)
    public String createBook(@Valid Book book, BindingResult result, Model model, RedirectAttributes attr,
                             @RequestParam(value="selectedCategory")String[] categories) {
        Message message = new Message();
        Set<Category> selectedCat = new HashSet<>();
        for(String s: categories){
            Long catId = Long.valueOf(s);
            Category cat = categoryService.findById(catId);
            selectedCat.add(cat);
        }
        book.setCategories(selectedCat);

        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.book", result);
            attr.addFlashAttribute("book", book);
            message.setError("Please correct the field errors.");
            attr.addFlashAttribute("message", message);
            return "redirect:/book/new";
        }
       /* if(bookService.titleAndAuthorValid(book) == false){
            //TODO redirect & INFO sodass erst nach 'yes' erstellt wird
        }*/
        Book createdBook = bookService.create(book);
        message.setSuccess("New Book added.");
        attr.addFlashAttribute("message", message);

        return "redirect:/book/" + createdBook.getId();
    }

    /**
     * FORM for EDIT book
     * In case of redirect model will contain "book"
     * @param id        book_id
     * @param model     attributeValues
     * @return          BOOK_EDIT_FORM_VIEW
     */
    @GetMapping("/book/{id}/edit")
    public String editBook(@PathVariable("id") long id, Model model) {
        Message message = new Message();
        Book book = bookService.findById(id);
        Set<Category> allCategories = categoryService.getCategories();

        if (!model.containsAttribute("book")) {
            model.addAttribute("book", book);
        } else{
            message.setError("Please correct the field values.");
        }
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("message", message);
        return BOOK_EDIT_FORM_VIEW;
    }

    /**
     * UPDATE article with field values from ARTICLE_EDIT_FORM_VIEW
     * After the redirect: flash attributes pass attributes to the model
     * @param id                book_id
     * @param bookDetails       entity
     * @param result            result of validation of field values from ARTICLE_ADD_FORM_VIEW
     * @param model             attributeValues
     * @param attr              stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/book/{bookId}/edit'
     *          else:      redirect: '/book/{bookId}'
     */
    @RequestMapping(path = "/book/{id}/update", method = RequestMethod.POST)
    public String updateBook(@PathVariable("id") long id, @Valid Book bookDetails,
                             BindingResult result, Model model, RedirectAttributes attr /*,
                             @RequestParam(value="selectedCategory")String[] categories*/){

        //filter categories from multiple select
        /*Set<Category> selectedCat = new HashSet<>();
        for(String s: categories){
            Long catId = Long.valueOf(s);
            Category cat = categoryService.findById(catId);
            selectedCat.add(cat);
        }
        bookDetails.setCategories(selectedCat);*/

        if (result.hasErrors() /*|| bookService.titleAndAuthorValid(bookDetails) == false*/) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.book", result);
            attr.addFlashAttribute("book", bookDetails);
            return "redirect:/article/" + bookDetails.getId() + "/edit";
        }
        bookService.update(id, bookDetails);
        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("msg", "Book has been updated.");
        return "redirect:/book/" + id;
    }

    /**
     * DELETE book by id from database
     * @param id            book_id
     * @param model         attributeValues
     * @return              redirect: '/books'
     */
    @RequestMapping(path = "/book/{id}/delete", method = RequestMethod.GET)
    public String deleteBook(@PathVariable("id") long id, Model model) {
        Book book = bookService.findById(id);
        String title = book.getTitle();
        bookService.delete(id);

        model.addAttribute("msg", "Book" + " '" + title + "' " + "has been deleted.");
        return "redirect:/books";
    }
}
