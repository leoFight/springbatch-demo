package com.example.springbatch.itemreaderxml;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: fly
 * @create: 2020-10-24 16:33
 **/
@Component("xmlFileWrite")
public class XmlFileWriter implements ItemWriter<Customer> {

    @Override
    public void write(List<? extends Customer> list) throws Exception {
        for (Customer customer : list){
            System.out.println(customer.toString());
        }
    }
}
