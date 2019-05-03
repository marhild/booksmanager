package com.example.booksmanager.support;

import com.example.booksmanager.domain.Book;
import com.example.booksmanager.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PageableView {

    private static final int BUTTONS_TO_SHOW = 3;
    private static final int INITIAL_PAGE = 0;
    private static final int INITIAL_PAGE_SIZE = 5;
    private static final int[] PAGE_SIZES = { 5, 10};

    private Optional<Integer> pageSize;
    private Optional<Integer> page;
    private CrudService crudService;
    private PagerModel pager;

    private int evalPageSize;
    private int evalPage;
    private Page<?> objectList;
    private Set<?> objectSet;

    public PageableView(Optional<Integer> pageSize, Optional<Integer> page, CrudService crudService) {
        this.pageSize = pageSize;
        this.page = page;
        this.crudService = crudService;
        this.evalPage();
        this.initWithService();
    }

    public PageableView(Optional<Integer> pageSize, Optional<Integer> page, Set<?> objectSet) {
        this.pageSize = pageSize;
        this.page = page;
        this.objectSet = objectSet;
        this.evalPage();
        this.initWithSet();
    }

    public void evalPage(){
        // If pageSize == null, return initial page size
        this.evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        this.evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
    }

    public void initWithService(){
        this.objectList = crudService.findAll(PageRequest.of(evalPage, evalPageSize));
        this.pager = new PagerModel(objectList.getTotalPages(),objectList.getNumber(),BUTTONS_TO_SHOW);

    }

    public void initWithSet(){
        List<?> listConverted = new ArrayList<>();
        listConverted.addAll((Set)objectSet);
        this.objectList = new PageImpl(listConverted, PageRequest.of(evalPage, evalPageSize), objectSet.size());
        this.pager = new PagerModel(objectList.getTotalPages(),objectList.getNumber(),BUTTONS_TO_SHOW);
    }

    public static int[] getPageSizes() {
        return PAGE_SIZES;
    }

    public PagerModel getPager() {
        return pager;
    }

    public int getEvalPageSize() {
        return evalPageSize;
    }

    public Page<?> getObjectList() {
        return objectList;
    }
}
