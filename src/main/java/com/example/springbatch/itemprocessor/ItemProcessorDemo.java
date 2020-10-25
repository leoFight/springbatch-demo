package com.example.springbatch.itemprocessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: fly
 * @create: 2020-10-25 18:53
 **/
@Configuration
@EnableBatchProcessing
public class ItemProcessorDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("dbJdbcReader")
    private ItemReader<? extends Customer> dbJdbcReader;
    @Autowired
    @Qualifier("dbFileWrite")
    private ItemWriter<? super Customer> dbFileWrite;
    @Autowired
    @Qualifier("firstNameUpperProcessor")
    private ItemProcessor<Customer,Customer> firstNameUpperProcessor;
    @Autowired
    @Qualifier("idFilterProcessor")
    private ItemProcessor<Customer,Customer> idFilterProcessor;

    @Bean
    public Job itemProcessorDemoJob(){
        return jobBuilderFactory.get("itemProcessorDemoJob")
                .start(itemProcessorDemoStep())
                .build();
    }

    @Bean
    public Step itemProcessorDemoStep() {
        return stepBuilderFactory.get("itemProcessorDemoStep")
                .<Customer,Customer>chunk(2)
                .reader(dbJdbcReader)
                //.processor(firstNameUpperProcessor)
                .processor(process())
                .writer(dbFileWrite)
                .build();
    }

    //多种数据的处理方式
    @Bean
    public CompositeItemProcessor<Customer,Customer> process(){
        CompositeItemProcessor<Customer, Customer> processor = new CompositeItemProcessor<>();
        List<ItemProcessor<Customer,Customer>> delagates = new ArrayList<>();
        delagates.add(firstNameUpperProcessor);
        delagates.add(idFilterProcessor);
        processor.setDelegates(delagates);
        return processor;
    }
}
