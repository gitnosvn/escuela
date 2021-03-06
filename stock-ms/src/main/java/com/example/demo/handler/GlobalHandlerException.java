package com.example.demo.handler;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.example.demo.exceptions.ErrorDetails;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.ValidacionException;

@ControllerAdvice
public class GlobalHandlerException {
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<?> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
		ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}
	

	@ExceptionHandler(ValidacionException.class)
	public ResponseEntity<?> validacionException(ValidacionException ex, WebRequest request) {
		ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMensaje(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.OK);
	}

}
