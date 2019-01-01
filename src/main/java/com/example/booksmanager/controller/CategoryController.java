package com.example.booksmanager.controller;

import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import com.example.booksmanager.service.BookService;
import com.example.booksmanager.service.CategoryService;
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

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author platoiscoding.com
 */
@Controller
public class CategoryController {

    //view templates
    protected static final String CATEGORY_VIEW = "categories/showCategory";                    //view template for single category
    protected static final String CATEGORY_ADD_FORM_VIEW = "categories/newCategory";            //form for new category
    protected static final String CATEGORY_EDIT_FORM_VIEW = "categories/editCategory";          //form for editing a category
    protected static final String CATEGORY_LIST_VIEW = "categories/allCategories";               //list view of category with pagination

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
     * converts Set<Book> into Page<Book>
     * @param id        category_id
     * @param model     attributeValues
     * @return          view template for single category
     *                  pageable list of books per category
     */
    @GetMapping("/category/{id}")
    public ModelAndView showSingleCategory(@PathVariable("id") long id, Model model,
                                     @RequestParam("pageSize") Optional<Integer> pageSize,
                                     @RequestParam("page") Optional<Integer> page) {
        ModelAndView modelAndView = new ModelAndView(CATEGORY_VIEW);
        Category category = categoryService.findById(id);
        Set<Book> books = categoryService.getBooksInCategory(category);

        Message message = new Message();
        if(books.isEmpty()){
            message.setInfo("There are no books in this category.");
        }
        //redirect
        if(!model.containsAttribute("message")){
            model.addAttribute("message", message);
        }
        // If pageSize == null, return initial page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        /*
            If page == null || page < 0 (to prevent exception), return initial size
            Else, return value of param. decreased by 1
        */
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        /*
            convert Set<Book> to Page<Book> with PageImpl(List<T> content, Pageable pageable, long total)
         */
        List<Book> booksConverted = new ArrayList<>();          //convert Set into List
        booksConverted.addAll(books);
        Page<Book> booksList = new PageImpl<Book>(booksConverted, PageRequest.of(evalPage, evalPageSize), books.size());
        PagerModel pager = new PagerModel(booksList.getTotalPages(),booksList.getNumber(),BUTTONS_TO_SHOW);

        modelAndView.addObject("booksList",booksList);
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        modelAndView.addObject("pager", pager);
        modelAndView.addObject("message", message);
        model.addAttribute("category", category);

        return modelAndView;
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
            message.setInfo("There are no categories in the database.");
        }
        // If pageSize == null, return initial page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        /*
            If page == null || page < 0 (to prevent exception), return initial size
            Else, return value of param. decreased by 1
        */
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;

        Page<Category> catList = categoryService.findAll(PageRequest.of(evalPage, evalPageSize));
        PagerModel pager = new PagerModel(catList.getTotalPages(),catList.getNumber(),BUTTONS_TO_SHOW);

        modelAndView.addObject("catList",catList);
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        modelAndView.addObject("pager", pager);
        modelAndView.addObject("message", message);

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
     * CREATE NEW category checks...
     *          (1)field values for errors
     *          (2)whether database already contains a category with the same name
     * After the redirect: flash attributes pass attributes to the model
     * @param category          entity
     * @param result        result of validation of field values from CATEGORY_ADD_FORM_VIEW
     * @param model         attributeValues
     * @param attr          stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/category/new'
     *          else:      redirect: '/category/{category_Id}'
     */
    @RequestMapping( path = "/category/create", method = RequestMethod.POST)
    public String createCategory(@Valid Category category, BindingResult result,
                                 Model model, RedirectAttributes attr) {
        Message message = new Message();
        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.category", result);
            attr.addFlashAttribute("category", category);
            message.setError("Please correct the field errors.");
            attr.addFlashAttribute("message", message);
            return "redirect:/category/new";
        }
        if(!categoryService.nameIsValid(category)){
            attr.addFlashAttribute("org.springframework.validation.BindingResult.category", result);
            attr.addFlashAttribute("category", category);
            message.setError("This category already exists.");
            attr.addFlashAttribute("message", message);
            return "redirect:/category/new";
        }
        Category createdCategory = categoryService.create(category);
        model.addAttribute("category", createdCategory);
        message.setSuccess("New Category added.");
        model.addAttribute("message", message);

        return "redirect:/category/" + createdCategory.getId();
    }

    /**
     * FORM for EDIT category
     * In case of redirect model will contain "category"
     * @param id        category_id
     * @param model     attributeValues
     * @return          CATEGORY_EDIT_FORM_VIEW
     */
    @GetMapping("/category/{id}/edit")
    public String editCategoryForm(@PathVariable("id") long id, Model model){
        Message message = new Message();
        Category category = categoryService.findById(id);

        if(!model.containsAttribute("category")){
            model.addAttribute("category", category);
        } else{
            message.setError("Please correct the field values.");
        }
        model.addAttribute("message", message);
        return CATEGORY_EDIT_FORM_VIEW;
    }

    /**
     * UPDATE category with field values from CATEGORY_EDIT_FORM_VIEW
     * After the redirect: flash attributes pass attributes to the model
     * @param id                    category_id
     * @param categoryDetails       entity
     * @param result                result of validation of field values from CATEGORY_EDIT_FORM_VIEW
     * @param model                 attributeValues
     * @param attr                  stores flash attributes; used when method returns a redirect view name
     * @return  if !valid: redirect: '/category/{categoty_id}/edit'
     *          else:      redirect: '/category/{categoty_id}'
     */
    @RequestMapping(path = "/category/{id}/update", method = RequestMethod.POST)
    public String updateCategory(@PathVariable("id") long id, @Valid Category categoryDetails,
                                 BindingResult result, Model model, RedirectAttributes attr) {
        Message message = new Message();
        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.category", result);
            attr.addFlashAttribute("category", categoryDetails);
            message.setError("Please correct the field errors.");
            attr.addFlashAttribute("message", message);
            return "redirect:/category/" + categoryDetails.getId() + "/edit";
        }
        /*if(categoryValid)*/ //TODO categoryName validieren, dann redirect wenn fail
        categoryService.update(id, categoryDetails);
        message.setSuccess("Category has been updated.");
        attr.addFlashAttribute("message", message);
        return "redirect:/category/" + id;
    }

    /**
     * DELETE book by id from database
     * @param id            category_id
     * @param model         attributeValues
     * @return              redirect: '/categories'
     */
    @RequestMapping(path = "/category/{id}/delete", method = RequestMethod.GET)
    public String deleteCategory(@PathVariable("id") long id, Model model) {
        Message message = new Message();
        categoryService.delete(id);
        message.setSuccess("Category has been deleted.");
        model.addAttribute("message", message);
        return "redirect:/categories";
        //TODO diese categories muss dann aus allen betroffenen Büchern gelöscht werden
    }
}
