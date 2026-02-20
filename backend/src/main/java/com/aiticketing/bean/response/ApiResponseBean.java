package com.aiticketing.bean.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseBean<T> {

	    private boolean success;
	    private String message;
	    private T data;
	    private List<ValidationError> errors;

	    public ApiResponseBean() {}

	    public ApiResponseBean(boolean success, String message, T data, List<ValidationError> errors) {
	        this.success = success;
	        this.message = message;
	        this.data = data;
	        this.errors = errors;
	    }

	    public static <T> ApiResponseBean<T> success(T data) {
	        return new ApiResponseBean<>(true, null, data, null);
	    }

	    public static <T> ApiResponseBean<T> success(String message, T data) {
	        return new ApiResponseBean<>(true, message, data, null);
	    }

	    public static <T> ApiResponseBean<T> failure(String message) {
	        return new ApiResponseBean<>(false, message, null, null);
	    }

	    public static <T> ApiResponseBean<T> failure(String message, List<ValidationError> errors) {
	        return new ApiResponseBean<>(false, message, null, errors);
	    }

	    public boolean isSuccess() { 
	    	return success; 
	    }
	    public void setSuccess(boolean success) { 
	    	this.success = success;
	    }
	    public String getMessage() { 
	    	return message; 
	    }
	    public void setMessage(String message) { 
	    	this.message = message; 
	    }
	    public T getData() { 
	    	return data; 
	    }
	    public void setData(T data) { 
	    	this.data = data; 
	    }
	    public List<ValidationError> getErrors() { 
	    	return errors; 
	    }
	    public void setErrors(List<ValidationError> errors) { 
	    	this.errors = errors; 
	    }
}
