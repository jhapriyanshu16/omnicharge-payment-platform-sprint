package com.omnicharge.operatorservice.exception;

public class OperatorNotFoundException extends RuntimeException {

    public OperatorNotFoundException(String msg){
        super(msg);
    }
}
