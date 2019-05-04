package com.example.booksmanager.controller;

import com.example.booksmanager.domain.Book;
import com.example.booksmanager.domain.Category;
import com.example.booksmanager.service.CategoryService;
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
// TODO entweder sind categories für bücher ein muss oder es gibt ein automatisches uncategorized das nicht gelöscht werden kann
//TODO man soll hier Bücher einfügen können: nur Liste von Büchern anzeigen (Multiselect) oder besser kästchen?
@Controller
public class CategoryController {

    //view templates
    protected static final String CATEGORY_VIEW = "categories/showCategory";                    //view template for single category
    protected static final String CATEGORY_ADD_FORM_VIEW = "categories/newCategory";            //form for new category
    protected static final String CATEGORY_EDIT_FORM_VIEW = "categories/editCategory";          //form for editing a category
    protected static final String CATEGORY_LIST_VIEW = "categories/allCategories";               //list view of category with pagination

    //messages
    protected static final String NEW_CATEGORY_SUCCESS = "New Category has been added.";
    protected static final String NO_CATEGORIES_IN_DB_INFO = "There are no Categories in the Database.";
    protected static final String CATEGORY_UPDATED_SUCCESS = "Category has been updated.";
    protected static final String CATEGORY_DELETED_SUCCESS = "Category has been deleted.";
    protected static final String NO_BOOKS_IN_THIS_CATEGORY_INFO = "There are no books in this Category.";
    protected static final String FIELD_VALIDATION_ERROR = "Please correct the field errors.";
    protected static final String NO_DUPLICATES_ALLOWED_ERROR = "A Category with the same Name already exists in the database.";


    @Autowired
    private CategoryService categoryService;
    @Autowired
    private Message message;
    @Autowired
    private PageModel pageModel;

    /**
     * GET category by id
     * Show all books in category
     * @param catId     category_id
     * @param model     attributeValues
     * @return          view template for single category
     *                  pageable list of books per category
     */
    @GetMapping("/category/{id}")
    public ModelAndView showSingleCategory(@PathVariable("id") long catId, Model model, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView(CATEGORY_VIEW);
        Category category = categoryService.findById(catId);
        Set<Book> books = category.getBooks();

        //TODO umstrukturieren
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if(/* if redirected*/ inputFlashMap != null){
            message = (Message) inputFlashMap.get("message");
            if(books.isEmpty()) message.setInfo(NO_BOOKS_IN_THIS_CATEGORY_INFO);
        }else{
            message = new Message();
            if(books.isEmpty()) message.setInfo(NO_BOOKS_IN_THIS_CATEGORY_INFO);
            model.addAttribute("message", message);
        }

        modelAndView.addObject("booksList",books);
        model.addAttribute("category", category);

        return modelAndView;
    }

    /**
     * GET all category from database
     * @return              list view of category
     */
    @RequestMapping("/categories")
    public ModelAndView showAllCategories(Model model,HttpServletRequest request) {
        if(!model.containsAttribute("message")){
            message.reset();
        }
        Set<Category>  allCategories = categoryService.getAll();
        ModelAndView modelAndView = new ModelAndView(CATEGORY_LIST_VIEW);

        //Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        //if(inputFlashMap != null){message = (Message) inputFlashMap.get("message");}
        if(allCategories.isEmpty()){message.setInfo(NO_CATEGORIES_IN_DB_INFO);}

        pageModel.initPageAndSize();
        modelAndView.addObject("categories", categoryService.findAll(PageRequest.of(pageModel.getPAGE(), pageModel.getSIZE())));
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
        if (!model.containsAttribute("category") || !model.containsAttribute("message")) {
            model.addAttribute("category", new Category());
            model.addAttribute("message", message);
        }
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

        if (result.hasErrors() || !categoryService.nameIsValid(category)) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.category", result);
            attr.addFlashAttribute("category", category);
            if(!categoryService.nameIsValid(category)){
                message.setError(NO_DUPLICATES_ALLOWED_ERROR);
            }else{
                message.setError(FIELD_VALIDATION_ERROR);
            }
            attr.addFlashAttribute("message", message);
            return "redirect:/category/new";
        }
        Category createdCategory = categoryService.create(category);
        model.addAttribute("category", createdCategory);
        message.setSuccess(NEW_CATEGORY_SUCCESS);
        attr.addFlashAttribute("message", message);

        return "redirect:/category/" + createdCategory.getId();
    }

    /**
     * FORM for EDIT category
     * In case of redirect model will contain "category" and "message"
     * @param catId        category_id
     * @param model     attributeValues
     * @return          CATEGORY_EDIT_FORM_VIEW
     */
    @GetMapping("/category/{id}/edit")
    public String editCategoryForm(@PathVariable("id") long catId, Model model){
        if(!model.containsAttribute("category") || !model.containsAttribute("message") ){
            Category category = categoryService.findById(catId);
            model.addAttribute("category", category);
            model.addAttribute("message", message);
        }
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
        //TODO neue function nameIsVAlid wobei id übergeben wird um den aktuellen namen zu pberspringen, sonst beschwert er sich beim speichern ohne änderungen, dass der name schon in der db existiert
        if (result.hasErrors() || !categoryService.nameIsValid(categoryDetails)) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.category", result);
            attr.addFlashAttribute("category", categoryDetails);

            if(!categoryService.nameIsValid(categoryDetails)){
                message.setError(NO_DUPLICATES_ALLOWED_ERROR);
            }else{
                message.setError(FIELD_VALIDATION_ERROR);
            }
            attr.addFlashAttribute("message", message);
            return "redirect:/category/" + categoryDetails.getId() + "/edit";
        }
        categoryService.update(id, categoryDetails);
        message.setSuccess(CATEGORY_UPDATED_SUCCESS);
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
        categoryService.delete(id);
        //TODo flashattr
        message.setSuccess(CATEGORY_DELETED_SUCCESS);
        model.addAttribute("message", message);
        return "redirect:/categories";
    }
}
