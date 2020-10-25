package com.example.springbatch.skip;

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
/*@Configuration
@EnableBatchProcessing*/
public class SkipDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("skipItemProcessor")
    private ItemProcessor<? super String, ? extends String> skipItemProcessor;
    @Autowired
    @Qualifier("skipItemWriter")
    private ItemWriter<? super String> skipItemWriter;

    @Bean
    public Job skipDemoJob(){
        return jobBuilderFactory.get("skipDemoJob")
                .start(skipDemoStep())
                .build();
    }

    @Bean
    public Step skipDemoStep() {
        return stepBuilderFactory.get("skipDemoStep")
                .<String,String>chunk(2)
                .reader(reader())
                .processor(skipItemProcessor)
                .writer(skipItemWriter)
                .faultTolerant()
                .skip(CustomSkipExcetion.class)
                .skipLimit(5)
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
