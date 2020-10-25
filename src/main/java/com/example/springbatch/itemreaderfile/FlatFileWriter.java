package com.example.springbatch.itemreaderfile;

import com.example.springbatch.itemreaderfile.Customer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: fly
 * @create: 2020-10-24 15:50
 **/
@Component("flatFileWriter")
public class FlatFileWriter implements ItemWriter<Customer> {
    @Override
    public void write(List<? extends Customer> list) throws Exception {
        for (Customer customer :list){
            System.out.println(customer.toString());
        }
    }
}
