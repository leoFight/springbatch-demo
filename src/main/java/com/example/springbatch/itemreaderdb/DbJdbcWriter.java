package com.example.springbatch.itemreaderdb;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: fly
 * @create: 2020-10-24 13:26
 **/
@Component("dbJdbcWriter")
public class DbJdbcWriter implements ItemWriter<User> {

    @Override
    public void write(List<? extends User> list) throws Exception {
        for (User user:list){
            System.out.println(user.toString());
        }
    }
}
