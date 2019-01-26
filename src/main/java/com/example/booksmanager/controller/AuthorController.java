package com.example.booksmanager.controller;

import com.example.booksmanager.domain.Author;
import com.example.booksmanager.service.AuthorService;
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

    @Autowired
    private AuthorService authorService;

    /**
     * GET author by id
     * After redirect from author/create: model contains attribute "message"(success)
     * @param id        author_id
     * @param model     attributeValues
     * @return          view template for single author
     */
    @RequestMapping( path = "/author/{id}")
    public String showSingleAuthor(@PathVariable("id") long id, Model model) {
        if(!model.containsAttribute("message")){
            Message message = new Message();
            model.addAttribute("message", message);
        }
        model.addAttribute("author", authorService.findById(id));
        return AUTHOR_VIEW;
    }

    /**
     * GET all authors from database
     * @param pageSize      number of authors per page
     * @param page          subset of all authors
     * @return              list view of authors
     */
    @RequestMapping({"/authors"})
    public ModelAndView showAllAuthorsWithPagination(@RequestParam("pageSize") Optional<Integer> pageSize,
                                                   @RequestParam("page") Optional<Integer> page) {

        ModelAndView modelAndView = new ModelAndView(AUTHOR_LIST_VIEW);
        Message message = new Message();
        // If pageSize == null, return initial page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        /*
            If page == null || page < 0 (to prevent exception), return initial size
            Else, return value of param. decreased by 1
        */
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;

        Page<Author> authorsList = authorService.findAll(PageRequest.of(evalPage, evalPageSize));
        if(authorsList.isEmpty()){
            message.setInfo("There are no authors in the database.");
        }
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
     * in case of redirection model will contain author
     * @param model     attributesValues
     * @return          AUTHOR_ADD_FORM_VIEW
     */
    @RequestMapping(path = "/author/new")
    public String newAuthorForm(Model model) {
        Message message = new Message();
        if (!model.containsAttribute("author")) {
            model.addAttribute("author", new Author());
        }else{
            message.setError("Please correct the field errors.");
        }
        model.addAttribute("message", message);

        return AUTHOR_ADD_FORM_VIEW;
    }

    /**
     * CREATE NEW author checks...
     *          (1)field values for errors
     *          (2)whether database already contains an author with the same name as field values
     * After the redirect: flash attributes pass attributes to the model
     * @param author          entity
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
            message.setError("Please correct the field errors.");
            attr.addFlashAttribute("message", message);
            return "redirect:/author/new";
        }
        /*if(authorService.newAuthorValid(author) == false){
            //TODO redirect & INFO sodass erst nach 'yes' erstellt wird
        }*/
        Author createdAuthor = authorService.create(author);
        message.setSuccess("New Author Added.");
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
            message.setError("Please correct the field values.");
        }
        //TODO author valid
        model.addAttribute("message", message);
        return AUTHOR_EDIT_FORM_VIEW;
    }
    //TODO delete book from showAuthor template

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
        //TODO if bedingung
        if (result.hasErrors() /*|| authorService.authorvalid(authorDetails) == false*/) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.author", result);
            attr.addFlashAttribute("author", authorDetails);
            message.setError("Please correct the field errors.");
            attr.addFlashAttribute("message", message);
            return "redirect:/author/" + authorDetails.getId() + "/edit";
        }
        authorService.update(id, authorDetails);
        message.setSuccess("Author has been updated.");
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
    public String deleteAuthor(@PathVariable("id") long id, Model model) {
        Message message = new Message();
        authorService.delete(id);
        message.setSuccess("Author has been deleted.");
        model.addAttribute("message", message);
        return "redirect:/authors";
        //TODO dieser Autor muss aus allen betroffenen categorien gelöscht werden
    }
}



