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
import java.util.Optional;
import java.util.Set;

@Controller
public class CategoryController {

    //view templates
    protected static final String CATEGORY_VIEW = "categories/showCategory";                    //view template for single category
    protected static final String CATEGORY_ADD_FORM_VIEW = "categories/newCategory";            //form for new category
    protected static final String CATEGORY_EDIT_FORM_VIEW = "categories/editCategory";          //form for editing a category
    protected static final String CATEGORY_LIST_VIEW = "categories/allCategory";               //list view of category with pagination

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
     * GET category by id
     * @param id        category_id
     * @param model     attributeValues
     * @return          view template for single category
     */
    @GetMapping("/category/{id}")
    public String showSingleCategory(@PathVariable("id") long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return CATEGORY_VIEW;
    }

    /**
     * GET all category from database
     * @param pageSize      number of category per page
     * @param page          subset of all category
     * @return              list view of category
     */
    @RequestMapping("/categories")
    public ModelAndView showAllCategoriesWithPagination(@RequestParam("pageSize") Optional<Integer> pageSize,
                                                  @RequestParam("page") Optional<Integer> page) {
        ModelAndView modelAndView = new ModelAndView(CATEGORY_LIST_VIEW);
        Set<Category> categories = categoryService.getCategories();
        Message message = new Message();
        if(categories.isEmpty()){
            message.setInfo("There are no books in the database.");
            modelAndView.addObject("message", message);
        }
        // If pageSize == null, return initial page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        /*
            If page == null || page < 0 (to prevent exception), return initial size
            Else, return value of param. decreased by 1
        */
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;

        Page<Category> categoryList = categoryService.findAll(PageRequest.of(evalPage, evalPageSize));
        PagerModel pager = new PagerModel(categoryList.getTotalPages(),categoryList.getNumber(),BUTTONS_TO_SHOW);

        modelAndView.addObject("booksList",categoryList);
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        modelAndView.addObject("pager", pager);

        return modelAndView;
    }

    /**
     * FORM for NEW article
     * in case of redirection model will contain category
     * @param model     attributeValues
     * @return          CATEGORY_ADD_FORM_VIEW
     */
    @RequestMapping(path = "/category/new")
    public String newCategoryForm(Model model) {

        Message message = new Message();
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new Category());
        }
        else{
            message.setError("Please correct the field errors.");
        }
        model.addAttribute("message", message);
        return CATEGORY_ADD_FORM_VIEW;
    }

    /**
     * CREATE NEW book checks...
     *          (1)field values for errors
     *          (2)whether database already contains a book with the same name and author as field values
     * After the redirect: flash attributes pass attributes to the model
     * @param category          entity
     * @param result        result of validation of field values from BOOK_ADD_FORM_VIEW
     * @param model         attributeValues
     * @param attr          stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/book/new'
     *          else:      redirect: '/book/{bookId}'
     */
    @RequestMapping( path = "/category/create", method = RequestMethod.POST)
    public String createCategory(@Valid Category category, BindingResult result, Model model, RedirectAttributes attr) {
        Message message = new Message();
        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.category", result);
            attr.addFlashAttribute("category", category);
            message.setError("Please correct the field errors.");
            attr.addFlashAttribute("message", message);
            return "redirect:/category/new";
        }
        Category createdCategory = categoryService.create(category);
        model.addAttribute("category", createdCategory);
        message.setSuccess("New Category added.");
        model.addAttribute("message", message);

        return "redirect:/category/" + createdCategory.getId();
    }

    /*---Get Books per Category---*/
    @GetMapping("/category/books/{id}")
    public String getBooksPerCategory(@PathVariable("id") long id, Model model) {
        Category category = categoryService.findById(id);
        Set<Book> books = category.getBooks();
        model.addAttribute("category", category);
        model.addAttribute("books", books);
        return "categories/showBooks";
    }

    /*---Update a category by id---*/
    @RequestMapping(path = "/category/{id}", method = RequestMethod.POST)
    public String update(@PathVariable("id") long id, Category category) {
        categoryService.update(id, category);
        return "redirect:/categories";    }

    /*---Delete a book by id---*/
    @RequestMapping(path = "/category/delete/{id}", method = RequestMethod.GET)
    public String delete(@PathVariable("id") long id) {
        categoryService.delete(id);
        return "redirect:/categories";
    }
}
