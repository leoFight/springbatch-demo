package com.example.springbatch.itemwriterxml;

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
 * @create: 2020-10-25 17:42
 **/

//@Configuration
//@EnableBatchProcessing
public class XmlItemWriterDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("dbJdbcReader")
    private ItemReader<? extends Customer> dbJdbcReader;
    @Autowired
    @Qualifier("xmlItemWriter")
    private ItemWriter<? super Customer> xmlItemWriter;

    @Bean
    public Job xmlItemWriterDemoJob(){
        return jobBuilderFactory.get("xmlItemWriterDemoJob")
                .start(xmlItemWriterDemoStep())
                .build();
    }

    private Step xmlItemWriterDemoStep() {
        return stepBuilderFactory.get("xmlItemWriterDemoStep")
                .<Customer,Customer>chunk(2)
                .reader(dbJdbcReader)
                .writer(xmlItemWriter)
                .build();
    }
}
