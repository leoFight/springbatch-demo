package com.example.springbatch.skiplistener;


import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * @author: fly
 * @create: 2020-10-25 19:41
 **/
@Component
public class SkipListenerItemProcessor implements ItemProcessor<String,String> {
    private int attemptCount = 0;

    @Override
    public String process(String s) throws Exception {
        System.out.println("processing s "+s);
        if (s.equalsIgnoreCase("26")){
            attemptCount++;
            if (attemptCount >= 3){
                System.out.println("Retried "+attemptCount+ " times success.");
                return String.valueOf(Integer.valueOf(s) * -1);
            }else{
                System.out.println("Processed the "+attemptCount+ " times fail");
                throw new CustomSkipExcetion("Process failed. Attempt："+attemptCount);
            }
        }else{
            return String.valueOf(Integer.valueOf(s) * -1);
        }
    }
}
