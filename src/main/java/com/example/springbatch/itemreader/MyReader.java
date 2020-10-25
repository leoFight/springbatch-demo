package com.example.springbatch.itemreader;


import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;
import java.util.List;

/**
 * @author: fly
 * @create: 2020-10-24 12:51
 **/

public class MyReader implements ItemReader<String> {
    private final Iterator<String> iterator;

    public MyReader(List<String> data) {
        this.iterator = data.iterator();
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (iterator.hasNext()) {
            return this.iterator.next();
        } else {
            return null;
        }
    }

}
