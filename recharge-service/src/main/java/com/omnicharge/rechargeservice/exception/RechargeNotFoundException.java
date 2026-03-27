package com.omnicharge.rechargeservice.exception;

public class RechargeNotFoundException extends RuntimeException {

    public RechargeNotFoundException(String msg){
        super(msg);
    }
}