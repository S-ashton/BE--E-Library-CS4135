package com.elibrary.book_service.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.elibrary.book_service.model.*;


public interface CopyRepository extends JpaRepository<BookCopy, Integer>{
      int countByIdAndStatus(Long id, Status status);
    
      Optional<BookCopy> findById(Long copy_id);

      Optional<List<BookCopy>> findByBookIdAndStatus(Long bookId, Status status);

      Optional<List<BookCopy>> findByBookId(Long bookId);
}
