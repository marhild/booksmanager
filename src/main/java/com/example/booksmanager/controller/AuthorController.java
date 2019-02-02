package com.example.booksmanager.controller;

import com.example.booksmanager.domain.Author;
import com.example.booksmanager.domain.Book;
import com.example.booksmanager.service.AuthorService;
import com.example.booksmanager.service.BookService;
import com.example.booksmanager.support.Message;
import com.example.booksmanager.support.PagerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
//TODO pfad für + new book durch author?
public class AuthorController {

    //view templates
    protected static final String AUTHOR_VIEW = "authors/showAuthor";                    //view template for single author
    protected static final String AUTHOR_ADD_FORM_VIEW = "authors/newAuthor";            //form for new author
    protected static final String AUTHOR_EDIT_FORM_VIEW = "authors/editAuthor";          //form for editing a author
    protected static final String AUTHOR_LIST_VIEW = "authors/allAuthors";               //list view of authors with pagination

    //pagination
    private static final int BUTTONS_TO_SHOW = 3;
    private static final int INITIAL_PAGE = 0;
    private static final int INITIAL_PAGE_SIZE = 5;
    private static final int[] PAGE_SIZES = { 5, 10};

    //messages
    protected static final String NEW_AUTHOR_SUCCESS = "New Author has been added.";
    protected static final String AUTHOR_UPDATED_SUCCESS = "Author has been updated.";
    protected static final String AUTHOR_DELETED_SUCCESS = "Author has been deleted.";
    protected static final String NO_BOOKS_BY_THIS_AUTHOR_INFO = "There are no book written by this Author.";
    protected static final String FIELD_VALIDATION_ERROR = "Plaese correct the field errors.";
    protected static final String NO_DUPLICATES_ALLOWED_ERROR = "An Author with the same Name already exists in the database.";

    @Autowired
    private AuthorService authorService;

    /**
     * GET author by id
     * After redirect from author/create: model contains attribute "message"(success)
     * nested table: books written by author
     * @param id        author_id
     * @param model     attributeValues
     * @return          view template for single author
     */
    @RequestMapping( path = "/author/{id}")
    public String showSingleAuthor(@PathVariable("id") long id, Model model,
                                   @RequestParam("pageSize") Optional<Integer> pageSize,
                                   @RequestParam("page") Optional<Integer> page, HttpServletRequest request) {
        Author author = authorService.findById(id);
        Set<Book> booksByAuthor = author.getBooks();
        List<Book> booksConverted = new ArrayList<>();
        booksConverted.addAll(booksByAuthor);

        // If pageSize == null, return initial page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;

        //convert Set<Book> to Page<Book>
        Page<Book> booksList = new PageImpl<Book>(booksConverted, PageRequest.of(evalPage, evalPageSize), booksByAuthor.size());
        PagerModel pager = new PagerModel(booksList.getTotalPages(),booksList.getNumber(),BUTTONS_TO_SHOW);

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if(/* if redirected*/ inputFlashMap != null){
            Message message = (Message) inputFlashMap.get("message");
            if(booksByAuthor.isEmpty()) message.setInfo(NO_BOOKS_BY_THIS_AUTHOR_INFO);
        }else{
            Message message = new Message();
            if(booksByAuthor.isEmpty()) message.setInfo(NO_BOOKS_BY_THIS_AUTHOR_INFO);
            model.addAttribute("message", message);
        }

        model.addAttribute("selectedPageSize", evalPageSize);
        model.addAttribute("booksList",booksList);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("author", authorService.findById(id));
        return AUTHOR_VIEW;
    }

    /**
     * GET all authors from database
     * With Pagination
     * @param pageSize      number of authors per page
     * @param page          subset of all authors
     * @return              list view of authors
     */
    @RequestMapping({"/authors"})
    public ModelAndView showAllAuthors(@RequestParam("pageSize") Optional<Integer> pageSize,
                                       @RequestParam("page") Optional<Integer> page,
                                       HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView(AUTHOR_LIST_VIEW);
        Message message = new Message();

        // If pageSize == null, return initial page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;

        //TODO wenn autor gelöscht wird muss success message hier auftauchen
        Page<Author> authorsList = authorService.findAll(PageRequest.of(evalPage, evalPageSize));

        if(authorsList.isEmpty()){
            message.setInfo(NO_BOOKS_BY_THIS_AUTHOR_INFO);
        }
        /*retrieve message from FlashAttribute
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) { Message message = (Message) inputFlashMap.get("message"); }
        *///muss umgeschrieben werden damit es funktioniert

        PagerModel pager = new PagerModel(authorsList.getTotalPages(),authorsList.getNumber(),BUTTONS_TO_SHOW);

        modelAndView.addObject("authorsList",authorsList);
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        modelAndView.addObject("pager", pager);
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
            Message message = new Message();
            model.addAttribute("message", message);
        }
        //retrieve message from FlashAttribute
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) { Message message = (Message) inputFlashMap.get("message"); }

        return AUTHOR_ADD_FORM_VIEW;
    }

    /**
     * CREATE NEW author checks...
     *          (1)field values for errors
     *          (2)whether database already contains an author with the same name as field values
     * After the redirect: flash attributes pass attributes to the model
     * @param author        entity
     * @param result        result of validation of field values from AUTHOR_ADD_FORM_VIEW
     * @param model         attributeValues
     * @param attr          stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/author/new'
     *          else:      redirect: '/author/{authorId}'
     */
    @RequestMapping(path = "/author/create", method = RequestMethod.POST)
    public String createAuthor(@Valid Author author, BindingResult result, Model model,
                             RedirectAttributes attr) {
        Message message = new Message();

        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.author", result);
            attr.addFlashAttribute("author", author);
            if(authorService.newAuthorValid(author) == false){
                message.setError(NO_DUPLICATES_ALLOWED_ERROR);
            }else{
                message.setError(FIELD_VALIDATION_ERROR);
            }
            attr.addFlashAttribute("message", message);
            return "redirect:/author/new";
        }
        Author createdAuthor = authorService.create(author);
        message.setSuccess(NEW_AUTHOR_SUCCESS);
        attr.addFlashAttribute("message", message);

        return "redirect:/author/" + createdAuthor.getId();
    }

    /**
     * FORM for EDIT author
     * In case of redirect model will contain "author"
     * @param id        author_id
     * @param model     attributeValues
     * @return          AUTHOR_EDIT_FORM_VIEW
     */
    @GetMapping("/author/{id}/edit")
    public String editAuthorForm(@PathVariable("id") long id, Model model) {
        Message message = new Message();
        Author author = authorService.findById(id);

        if (!model.containsAttribute("author")) {
            model.addAttribute("author", author);
        } else{
            message.setError(FIELD_VALIDATION_ERROR);
        }
        //TODO author valid
        model.addAttribute("message", message);
        return AUTHOR_EDIT_FORM_VIEW;
    }

    /**
     * UPDATE author with field values from AUTHOR_EDIT_FORM_VIEW
     * After the redirect: flash attributes pass attributes to the model
     * @param id                author_id
     * @param authorDetails     entity
     * @param result            result of validation of field values from AUTHOR_EDIT_FORM_VIEW
     * @param model             attributeValues
     * @param attr              stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/author/{authorId}/edit'
     *          else:      redirect: '/author/{authorId}'
     */
    @RequestMapping(path = "/author/{id}/update", method = RequestMethod.POST)
    public String updateAuthor(@PathVariable("id") long id, @Valid Author authorDetails,
                             BindingResult result, Model model, RedirectAttributes attr){

        Message message = new Message();

        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.author", result);
            attr.addFlashAttribute("author", authorDetails);
            if(authorService.newAuthorValid(authorDetails) == false){
                message.setError(NO_DUPLICATES_ALLOWED_ERROR);
            }else{
                message.setError(FIELD_VALIDATION_ERROR);
            }
            attr.addFlashAttribute("message", message);
            return "redirect:/author/" + authorDetails.getId() + "/edit";
        }

        authorService.update(id, authorDetails);
        message.setSuccess(AUTHOR_UPDATED_SUCCESS);
        attr.addFlashAttribute("message", message);
        return "redirect:/author/" + id;
    }

    /**
     * DELETE author by id from database
     * @param id            author_id
     * @param model         attributeValues
     * @return              redirect: '/authors'
     */
    @RequestMapping(path = "/author/{id}/delete", method = RequestMethod.GET)
    public String deleteAuthor(@PathVariable("id") long id, Model model, RedirectAttributes attr) {
        Message message = new Message();
        authorService.delete(id);
        message.setSuccess(AUTHOR_DELETED_SUCCESS);
        attr.addFlashAttribute("message", message);
        return "redirect:/authors";
        //TODO werden dann auch alle betroffenen Bücher gelöscht? NEIN! beheben!! PopUp bei Löschversuch!!
        //bei removeBookFromCategory functs: abgucken
    }
}



