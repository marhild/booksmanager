package com.example.booksmanager.controller;

import com.example.booksmanager.domain.Author;
import com.example.booksmanager.domain.Book;
import com.example.booksmanager.service.AuthorService;
import com.example.booksmanager.service.BookService;
import com.example.booksmanager.support.Message;
import com.example.booksmanager.support.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

/**
 * @author platoiscoding.com
 */
@Controller
public class AuthorController {

    //view templates
    protected static final String AUTHOR_VIEW = "authors/showAuthor";                    //view template for single author
    protected static final String AUTHOR_ADD_FORM_VIEW = "authors/newAuthor";            //form for new author
    protected static final String AUTHOR_EDIT_FORM_VIEW = "authors/editAuthor";          //form for editing a author
    protected static final String AUTHOR_LIST_VIEW = "authors/allAuthors";               //list view of authors with pagination

    //messages
    protected static final String NEW_AUTHOR_SUCCESS = "New Author has been added.";
    protected static final String NO_AUTHORS_IN_DB_INFO = "There are no Authors in the Database.";
    protected static final String AUTHOR_UPDATED_SUCCESS = "Author has been updated.";
    protected static final String AUTHOR_DELETED_SUCCESS = "Author has been deleted.";
    protected static final String NO_BOOKS_BY_THIS_AUTHOR_INFO = "There are no books written by this Author.";
    protected static final String FIELD_VALIDATION_ERROR = "Please correct the field errors.";
    protected static final String AUTHOR_ALREADY_EXISTS = "An Author with the same Name already exists in the database.";

    @Autowired
    private AuthorService authorService;
    @Autowired
    private BookService bookService;
    @Autowired
    private Message message;
    @Autowired
    private PageModel pageModel;

    /**
     * GET author by id + show all booksByAuthor
     * After redirect from author/create: model contains attribute "message"(success)
     * nested table: books written by author
     * @param authorId        author_id
     * @param model     attributeValues
     * @return          view template for single author
     */
    @RequestMapping( path = "/author/{id}")
    public String showSingleAuthor(@PathVariable("id") long authorId, Model model) {
        Author author = authorService.findById(authorId);
        Set<Book> booksByAuthor = author.getBooks();

        if(booksByAuthor.isEmpty()) message.setInfo(NO_BOOKS_BY_THIS_AUTHOR_INFO);

        pageModel.initPageAndSize();
        model.addAttribute("books", bookService.findAllByAuthors(author, PageRequest.of(pageModel.getPAGE(), pageModel.getSIZE())));
        model.addAttribute("author", author);
        model.addAttribute("message", message);
        return AUTHOR_VIEW;
    }

    /**
     * GET all authors from database
     * If redirected from /delete, contains FlashAttribute "message"
     * With Pagination
     * @return              list view of authors
     */
    @RequestMapping({"/authors"})
    public ModelAndView showAllAuthors(Model model, HttpServletRequest request ) {

        if(!model.containsAttribute("message")){ message.reset(); }
        if(authorService.getAll().isEmpty()) message.setInfo(NO_AUTHORS_IN_DB_INFO);

        ModelAndView modelAndView = new ModelAndView(AUTHOR_LIST_VIEW);

        pageModel.initPageAndSize();
        modelAndView.addObject("authors", authorService.findAll(PageRequest.of(pageModel.getPAGE(), pageModel.getSIZE())));
        modelAndView.addObject("message", message);
        return modelAndView;
    }

    /**
     * FORM for NEW Author
     * if redirected from createAuthor: model contains FlashAttributes "author" and "message"
     * @param model     attributesValues
     * @return          AUTHOR_ADD_FORM_VIEW
     */
    @RequestMapping(path = "/author/new")
    public String newAuthorForm(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("author")) {
            model.addAttribute("author", new Author());
        }
        //retrieve message from FlashAttribute
        //Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        //if (inputFlashMap != null) { message = (Message) inputFlashMap.get("message"); }
        model.addAttribute("message", message);
        return AUTHOR_ADD_FORM_VIEW;
    }

    /**
     * CREATE NEW author checks...
     *          (1)field values for errors
     *          (2)whether database already contains an author with the same name as field values
     * After the redirect: flash attributes pass attributes to the model
     * @param author        entity
     * @param result        result of validation of field values from AUTHOR_ADD_FORM_VIEW
     * @param attr          stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/author/new'
     *          else:      redirect: '/author/{authorId}'
     */
    @RequestMapping(path = "/author/create", method = RequestMethod.POST)
    public String createAuthor(@Valid Author author, BindingResult result,
                             RedirectAttributes attr) {
        message.reset();
        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.author", result);
            attr.addFlashAttribute("author", author);
            if(!authorService.authorNameValid(author)){
                message.setError(AUTHOR_ALREADY_EXISTS);
            }else{
                message.setError(FIELD_VALIDATION_ERROR);
            }
            //attr.addFlashAttribute("message", message);
            return "redirect:/author/new";
        }
        Author createdAuthor = authorService.create(author);

        message.setSuccess(NEW_AUTHOR_SUCCESS);
        //attr.addFlashAttribute("message", message);

        return "redirect:/author/" + createdAuthor.getId();
    }

    /**
     * FORM for EDIT author
     * In case of redirect model will contain "author"
     * @param authorId        author_id
     * @param model     attributeValues
     * @return          AUTHOR_EDIT_FORM_VIEW
     */
    @GetMapping("/author/{id}/edit")
    public String editAuthorForm(@PathVariable("id") long authorId, Model model) {
        if (!model.containsAttribute("author")) {
            model.addAttribute("author", authorService.findById(authorId));
        }
        //message.reset();
        model.addAttribute("message", message);
        return AUTHOR_EDIT_FORM_VIEW;
    }

    /**
     * UPDATE author with field values from AUTHOR_EDIT_FORM_VIEW
     * After the redirect: flash attributes pass attributes to the model
     * @param authorId          author_id
     * @param authorDetails     entity
     * @param result            result of validation of field values from AUTHOR_EDIT_FORM_VIEW
     * @param attr              stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/author/{authorId}/edit'
     *          else:      redirect: '/author/{authorId}'
     */
    @RequestMapping(path = "/author/{id}/update", method = RequestMethod.POST)
    public String updateAuthor(@PathVariable("id") long authorId, @Valid Author authorDetails,
                             BindingResult result, RedirectAttributes attr){

        if (result.hasErrors() || !authorService.authorNameValid(authorDetails)) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.author", result);
            attr.addFlashAttribute("author", authorDetails);
            if(!authorService.authorNameValid(authorDetails)){
                message.setError(AUTHOR_ALREADY_EXISTS);
            }else{
                message.setError(FIELD_VALIDATION_ERROR);
            }
            //attr.addFlashAttribute("message", message);
            return "redirect:/author/" + authorId + "/edit";
        }

        authorService.update(authorId, authorDetails);
        message.setSuccess(AUTHOR_UPDATED_SUCCESS);
        attr.addFlashAttribute("message", message);
        return "redirect:/author/" + authorId;
    }

    /**
     * DELETE author by id from database
     * @param authorId            author_id
     * @return              redirect: '/authors'
     */
    @RequestMapping(path = "/author/{id}/delete", method = RequestMethod.GET)
    public String deleteAuthor(@PathVariable("id") long authorId, RedirectAttributes attr) {
        authorService.delete(authorId);
        message.setSuccess(AUTHOR_DELETED_SUCCESS);
        //attr.addFlashAttribute("message", message);
        return "redirect:/authors";
        //TODO werden dann auch alle betroffenen Bücher gelöscht? NEIN! beheben!! PopUp bei Löschversuch!!
        //TODO bei removeBookFromCategory functs: abgucken
    }
}



