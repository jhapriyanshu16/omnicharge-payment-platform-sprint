package com.omnicharge.operatorservice.exception;

public class PlanAlreadyExistsException extends RuntimeException {

    public PlanAlreadyExistsException(String msg){
        super(msg);
    }
}