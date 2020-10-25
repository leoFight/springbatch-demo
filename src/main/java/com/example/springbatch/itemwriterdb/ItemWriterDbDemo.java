package com.example.springbatch.itemwriterdb;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: fly
 * @create: 2020-10-25 15:47
 **/

//@Configuration
//@EnableBatchProcessing
public class ItemWriterDbDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("flatFileReader")
    private ItemReader<Customer> flatFileReader;
    @Autowired
    @Qualifier("itemWriterDb")
    private ItemWriter<? super Customer> itemWriterDb;

    @Bean
    public Job ItemWriterDbDemoJob(){
        return jobBuilderFactory.get("ItemWriterDbDemoJob")
                .start(ItemWriterDbDemoStep())
                .build();
    }

    @Bean
    public Step ItemWriterDbDemoStep() {
        return stepBuilderFactory.get("ItemWriterDbDemoStep")
                .<Customer,Customer>chunk(2)
                .reader(flatFileReader)
                .writer(itemWriterDb)
                .build();
    }
}
