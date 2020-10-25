package com.example.springbatch.retry;

/**
 * @author: fly
 * @create: 2020-10-25 19:46
 **/

public class CustomRetryExcetion extends Exception {
    public CustomRetryExcetion() {
        super();
    }
    public CustomRetryExcetion(String msg){
        super(msg);
    }
}
