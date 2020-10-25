package com.example.springbatch.retry;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: fly
 * @create: 2020-10-25 19:39
 **/
@Component
public class RetryItemWriter implements ItemWriter<String> {
    @Override
    public void write(List<? extends String> list) throws Exception {
        for (String item: list){
            System.out.println(item);
        }
    }
}
