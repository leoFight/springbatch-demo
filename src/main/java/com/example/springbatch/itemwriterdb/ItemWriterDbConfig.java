package com.example.springbatch.itemwriterdb;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author: fly
 * @create: 2020-10-25 16:02
 **/

@Configuration
public class ItemWriterDbConfig {
    @Autowired
    private DataSource dataSource;
    @Bean
    public JdbcBatchItemWriter<Customer> itemWriterDb(){
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("insert into CUSTOMER(id,firstName,lastName,birthday) values(:id,:firstName,:lastName,:birthday)");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Customer>());
        return writer;
    }
}
