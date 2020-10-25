package com.example.springbatch.itemreader;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @author: fly
 * @create: 2020-10-24 12:43
 **/

//@Configuration
//@EnableBatchProcessing
public class ItemReaderDemo {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job itemReaderDemoJob(){
        return jobBuilderFactory.get("itemReaderDemoJob")
                .start(itemReaderDemoStep())
                .build();
    }

    @Bean
    public Step itemReaderDemoStep() {
        return stepBuilderFactory.get("itemReaderDemoStep")
                .<String,String>chunk(2)
                .reader(itemReadDemoRead())
                .writer(list -> {
                    for (String item: list){
                        System.out.println(item+"...");
                    }
                })
                .build();
    }

    @Bean
    public MyReader itemReadDemoRead() {
        List<String> data = Arrays.asList("cat","doc","pig","duck");
        return new MyReader(data);
    }
}
