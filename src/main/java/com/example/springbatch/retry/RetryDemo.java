package com.example.springbatch.retry;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: fly
 * @create: 2020-10-25 19:33
 **/
//@Configuration
//@EnableBatchProcessing
public class RetryDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("retryItemProcessor")
    private ItemProcessor<? super String, ? extends String> retryItemProcessor;
    @Autowired
    @Qualifier("retryItemWriter")
    private ItemWriter<? super String> retryItemWriter;

    @Bean
    public Job retryDemoJob(){
        return jobBuilderFactory.get("retryDemoJob")
                .start(retryDemoStep())
                .build();
    }

    @Bean
    public Step retryDemoStep() {
        return stepBuilderFactory.get("retryDemoStep")
                .<String,String>chunk(2)
                .reader(reader())
                .processor(retryItemProcessor)
                .writer(retryItemWriter)
                .faultTolerant()//容错
                .retry(CustomRetryExcetion.class)
                .retryLimit(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<String> reader() {
        List<String> list = new ArrayList<>();
        for (int i =0; i <60; i++){
            list.add(String.valueOf(i));
        }
        return new ListItemReader<>(list);
    }

}
