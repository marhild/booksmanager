package com.example.booksmanager.controller;


import com.example.booksmanager.domain.Author;
import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import com.example.booksmanager.service.AuthorService;
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
import java.util.Optional;
import java.util.Set;

/**
 * @author platoiscoding.com
 */
//TODO SQL queries in repositories müssen getestet werden/funken bestimmt nicht
    //TODO bei newBook muss ein error auftauchen, wenn es keine Autoren oder Categories gibt, da sonst keine Bücher erstellt werden können
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

    //messages
    protected static final String NEW_BOOK_SUCCESS = "New BOOK has been added.";
    protected static final String NO_BOOKS_IN_DB_INFO = "There are no Books in the Database.";
    protected static final String BOOK_UPDATED_SUCCESS = "Book has been updated.";
    protected static final String BOOK_DELETED_SUCCESS = "Book has been deleted.";
    protected static final String FIELD_VALIDATION_ERROR = "Please correct the field errors.";
    protected static final String NO_DUPLICATES_ALLOWED_ERROR = "A Book with the same title already exists in the database.";


    @Autowired
    private BookService bookService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AuthorService authorService;

    /**
     * GET book by id
     * After redirect from book/create: model contains attribute "message"(success)
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
    public ModelAndView showAllBooks(@RequestParam("pageSize") Optional<Integer> pageSize,
                                                   @RequestParam("page") Optional<Integer> page) {
        ModelAndView modelAndView = new ModelAndView(BOOK_LIST_VIEW);
        Message message = new Message();

        // If pageSize == null, return initial page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;

        Page<Book> booksList = bookService.findAll(PageRequest.of(evalPage, evalPageSize));
        if(booksList.isEmpty()){
            message.setInfo(NO_BOOKS_IN_DB_INFO);
        }
        PagerModel pager = new PagerModel(booksList.getTotalPages(),booksList.getNumber(),BUTTONS_TO_SHOW);

        modelAndView.addObject("booksList",booksList);
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        modelAndView.addObject("pager", pager);
        modelAndView.addObject("message", message);

        return modelAndView;
    }

    /**
     * FORM for NEW Book
     * in case of redirection model will contain book
     * @param model     attributesValues
     * @return          BOOK_ADD_FORM_VIEW
     */
    @RequestMapping(path = "/book/new")
    public String newBookForm(Model model) {

        Message message = new Message();
        if (!model.containsAttribute("book")) {
            model.addAttribute("book", new Book());
        }else{
            /*redirected*/
            message.setError(FIELD_VALIDATION_ERROR);
        }
        Set<Category> allCategories = categoryService.getCategories();
        model.addAttribute("allCategories", allCategories);
        Set<Author> allAuthors = authorService.getAuthors();
        model.addAttribute("allAuthors", allAuthors);

        if(allCategories.isEmpty() || allAuthors.isEmpty()){
            message.setInfo("First, create at least one Category and one Author to add a new Book.");
        }
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
    public String createBook(@Valid Book book, BindingResult result, Model model, RedirectAttributes attr) {
        Message message = new Message();

        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.book", result);
            attr.addFlashAttribute("book", book);
            message.setError(FIELD_VALIDATION_ERROR);
            attr.addFlashAttribute("message", message);
            return "redirect:/book/new";
        }
       /* if(bookService.titleValid(book) == false){
            //TODO redirect & INFO sodass erst nach 'yes' erstellt wird
        }*/
        Book createdBook = bookService.create(book);
        message.setSuccess(NEW_BOOK_SUCCESS);
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
    public String editBookForm(@PathVariable("id") long id, Model model) {
        Message message = new Message();
        Book book = bookService.findById(id);
        Set<Category> allCategories = categoryService.getCategories();
        Set<Author> allAuthors = authorService.getAuthors();

        if (!model.containsAttribute("book")) {
            model.addAttribute("book", book);
        } else{
            message.setError(FIELD_VALIDATION_ERROR);
        }
        //TODO title valid
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("allAuthors", allAuthors);
        model.addAttribute("message", message);
        return BOOK_EDIT_FORM_VIEW;
    }

    /**
     * UPDATE book with field values from BOOK_EDIT_FORM_VIEW
     * After the redirect: flash attributes pass attributes to the model
     * @param id                book_id
     * @param bookDetails       entity
     * @param result            result of validation of field values from BOOK_EDIT_FORM_VIEW
     * @param model             attributeValues
     * @param attr              stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/book/{bookId}/edit'
     *          else:      redirect: '/book/{bookId}'
     */
    @RequestMapping(path = "/book/{id}/update", method = RequestMethod.POST)
    public String updateBook(@PathVariable("id") long id, @Valid Book bookDetails,
                             BindingResult result, Model model, RedirectAttributes attr){
        Message message = new Message();
        //TODO if bedingung
        if (result.hasErrors() /*|| bookService.titleValid(bookDetails) == false*/) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.book", result);
            attr.addFlashAttribute("book", bookDetails);
            message.setError(FIELD_VALIDATION_ERROR);
            attr.addFlashAttribute("message", message);
            return "redirect:/book/" + bookDetails.getId() + "/edit";
        }
        bookService.update(id, bookDetails);
        message.setSuccess(BOOK_UPDATED_SUCCESS);
        attr.addFlashAttribute("message", message);
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
        Message message = new Message();
        bookService.delete(id);
        //TODO flashmessage
        message.setSuccess(BOOK_DELETED_SUCCESS);
        model.addAttribute("message", message);
        return "redirect:/books";
    }

    /**
     * REMOVE a book from a category
     * @param bookId        book_id
     * @param catId         category_id
     * @param model         attributeValues
     * @return              redirect: '/categories'
     */
    @RequestMapping(path = "/book/{bookId}/removeFromCategory/{catId}", method = RequestMethod.GET)
    public String removeBookFromCategory(@PathVariable("bookId") long bookId, @PathVariable("catId") long catId,
                                         Model model, RedirectAttributes attr) {
        Message message = new Message();
        Book book = bookService.findById(bookId);
        Category category = categoryService.findById(catId);

        if(book.getCategories().size() < 2){
            message.setError("Couldn't remove Book. A Book must have at least one Category.");
            attr.addFlashAttribute("message", message);
            return "redirect:/category/" + category.getId();
        }

        bookService.removeFromCategory(book, category);
        message.setSuccess(book.getTitle() +" has been deleted from " + category.getName() +".");
        model.addAttribute("message", message);
        return "redirect:/category/" + category.getId();
    }
}
