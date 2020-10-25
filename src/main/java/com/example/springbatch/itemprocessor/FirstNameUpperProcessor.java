package com.example.springbatch.itemprocessor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;


/**
 * @author: fly
 * @create: 2020-10-25 18:58
 **/
@Component
public class FirstNameUpperProcessor  implements ItemProcessor<Customer,Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {
        Customer customer1 = new Customer();
        customer1.setId(customer.getId());
        customer1.setFirstName(customer.getFirstName().toUpperCase());
        customer1.setLastName(customer.getLastName());
        customer1.setBirthday(customer.getBirthday());
        return customer1;
    }
}
