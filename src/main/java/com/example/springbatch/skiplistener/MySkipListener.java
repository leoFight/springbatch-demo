package com.example.springbatch.skiplistener;

import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

/**
 * @author: fly
 * @create: 2020-10-25 20:16
 **/
@Component
public class MySkipListener implements SkipListener<String,String> {
    @Override
    public void onSkipInRead(Throwable throwable) {

    }

    @Override
    public void onSkipInWrite(String s, Throwable throwable) {

    }

    @Override
    public void onSkipInProcess(String s, Throwable throwable) {
        System.out.println(s +" occur exection "+ throwable);
    }
}
