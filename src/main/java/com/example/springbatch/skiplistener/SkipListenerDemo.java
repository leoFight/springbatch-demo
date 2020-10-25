package com.example.springbatch.skiplistener;

import org.springframework.batch.core.ItemProcessListener;
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
@Configuration
@EnableBatchProcessing
public class SkipListenerDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("skipListenerItemProcessor")
    private ItemProcessor<? super String, ? extends String> skipListenerItemProcessor;
    @Autowired
    @Qualifier("skipListenerItemWriter")
    private ItemWriter<? super String> skipListenerItemWriter;
    @Autowired
    @Qualifier("mySkipListener")
    private MySkipListener mySkipListener;

    @Bean
    public Job skipListenerDemoJob(){
        return jobBuilderFactory.get("skipListenerDemoJob")
                .start(skipListenerDemoStep1())
                .build();
    }

    @Bean
    public Step skipListenerDemoStep1() {
        return stepBuilderFactory.get("skipListenerDemoStep1")
                .<String,String>chunk(2)
                .reader(reader())
                .processor(skipListenerItemProcessor)
                .writer(skipListenerItemWriter)
                .faultTolerant()
                .skip(CustomSkipExcetion.class)
                .skipLimit(10)
                .listener(mySkipListener)
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
