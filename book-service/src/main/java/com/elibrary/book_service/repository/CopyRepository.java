package com.elibrary.book_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.elibrary.book_service.model.*;


public interface CopyRepository extends JpaRepository<BookCopy, Integer>{
      int countByIdAndAvailability(Long id, Status status);
    
      BookCopy findByIdLong(Long copy_id);
}
