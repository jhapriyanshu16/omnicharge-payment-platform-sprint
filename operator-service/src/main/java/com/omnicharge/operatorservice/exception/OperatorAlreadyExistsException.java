package com.omnicharge.operatorservice.exception;

public class OperatorAlreadyExistsException extends RuntimeException {

    public OperatorAlreadyExistsException(String msg){
        super(msg);
    }
}
