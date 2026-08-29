package com.dineflow.api.exception;

import com.dineflow.application.exception.ConflictException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflict(ConflictException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<String> handleOptimisticLock(
            OptimisticLockException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Reservation was modified by another request. Please try again.");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Reservation was modified by another request. Please try again.");
    }


}