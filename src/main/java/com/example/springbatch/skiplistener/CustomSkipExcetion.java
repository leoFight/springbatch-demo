package com.example.springbatch.skiplistener;

/**
 * @author: fly
 * @create: 2020-10-25 19:46
 **/

public class CustomSkipExcetion extends Exception {
    public CustomSkipExcetion() {
        super();
    }
    public CustomSkipExcetion(String msg){
        super(msg);
    }
}
