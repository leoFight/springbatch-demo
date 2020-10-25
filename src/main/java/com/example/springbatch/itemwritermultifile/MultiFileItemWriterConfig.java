package com.example.springbatch.itemwritermultifile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: fly
 * @create: 2020-10-25 18:12
 **/
@Configuration
public class MultiFileItemWriterConfig {

    @Bean
    public FlatFileItemWriter<Customer> jsonFileWriter() throws Exception {
        //把Customer对象转换成字符串输出到文件
        FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<>();
        //本地文件路径
        writer.setResource(new FileSystemResource("f:\\1.txt"));
        //把Customer对象转换成字符串
        writer.setLineAggregator(new LineAggregator<Customer>() {
            @Override
            public String aggregate(Customer customer) {
                ObjectMapper mapper = new ObjectMapper();
                String str = null;
                try {
                    str = mapper.writeValueAsString(customer);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return str;
            }
        });
        writer.afterPropertiesSet();
        return writer;
    }


    @Bean
    public StaxEventItemWriter<Customer> xmlFileWriter() throws Exception {
        StaxEventItemWriter<Customer> writer = new StaxEventItemWriter<>();

        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String,Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);
        marshaller.setAliases(aliases);

        writer.setRootTagName("customers");
        writer.setMarshaller(marshaller);

        String path ="f:\\1.xml";
        writer.setResource(new FileSystemResource(path));
        writer.afterPropertiesSet();
        return writer;
    }
    //输出数据到多个文件
 /*   @Bean
    public CompositeItemWriter<Customer> multiFileWriter() throws Exception {
        CompositeItemWriter<Customer> writer = new CompositeItemWriter<>();
        writer.setDelegates(Arrays.asList(jsonFileWriter(),xmlFileWriter()));
        writer.afterPropertiesSet();
        return writer;
    }*/

    //实现分类
    @Bean
    public ClassifierCompositeItemWriter<Customer> multiFileWriter(){
        ClassifierCompositeItemWriter<Customer> writer = new ClassifierCompositeItemWriter<>();

        writer.setClassifier(new Classifier<Customer, ItemWriter<? super Customer>>() {
            @Override
            public ItemWriter<? super Customer> classify(Customer customer) {
                //按照Customer的id进行分类
                ItemWriter<Customer> writer = null;
                try {
                    writer = customer.getId() % 2 == 0 ? jsonFileWriter():xmlFileWriter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return writer;
            }
        });

        return writer;
    }
}
