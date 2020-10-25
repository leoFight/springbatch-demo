package com.example.springbatch.itemprocessor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;


/**
 * @author: fly
 * @create: 2020-10-25 18:58
 **/
@Component
public class IdFilterProcessor implements ItemProcessor<Customer,Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {
        if (customer.getId() % 2 ==0 ){
            return customer;
        }else {
            return null;
        }
    }
}
