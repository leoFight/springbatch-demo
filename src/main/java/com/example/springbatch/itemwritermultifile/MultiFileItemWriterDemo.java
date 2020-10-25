package com.example.springbatch.itemwritermultifile;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: fly
 * @create: 2020-10-25 18:04
 **/
//@Configuration
//@EnableBatchProcessing
public class MultiFileItemWriterDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("dbJdbcReader")
    private ItemReader<? extends Customer> dbJdbcReader;
    @Autowired
    @Qualifier("multiFileWriter")
    private ItemWriter<? super Customer> multiFileWriter;
    @Autowired
    @Qualifier("jsonFileWriter")
    private ItemWriter<Customer> jsonFileWriter;
    @Autowired
    @Qualifier("xmlFileWriter")
    private ItemWriter<Customer> xmlFileWriter;

    @Bean
    public Job multiFileItemWriterDemoJob(){
        return jobBuilderFactory.get("multiFileItemWriterDemoJob1")
                .start(multiFileItemWriterDemoStep())
                .build();
    }

    @Bean
    public Step multiFileItemWriterDemoStep() {
        return stepBuilderFactory.get("multiFileItemWriterDemoStep")
                .<Customer,Customer>chunk(2)
                .reader(dbJdbcReader)
                .writer(multiFileWriter)
                .stream((ItemStream) jsonFileWriter)
                .stream((ItemStream) xmlFileWriter)
                .build();
    }
}
