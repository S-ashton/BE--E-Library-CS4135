package com.elibrary.book_service.service;

import com.elibrary.book_service.dto.AddNewTitleRequest;
import com.elibrary.book_service.dto.IndividualTitleResponse;

import jakarta.transaction.Transactional;

public class BookService {
    //addTitle
    //addCopy
    //changeStatus
    //getTitle
    //search

    @Transactional
    public IndividualTitleResponse addTitle(AddNewTitleRequest title){
        boolean duplicateExists = 
    }
}
