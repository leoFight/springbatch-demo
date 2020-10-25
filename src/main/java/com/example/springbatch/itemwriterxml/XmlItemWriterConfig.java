package com.example.springbatch.itemwriterxml;

import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: fly
 * @create: 2020-10-25 17:46
 **/
//@Configuration
public class XmlItemWriterConfig {
    @Bean
    public StaxEventItemWriter<Customer> xmlItemWriter() throws Exception {
        StaxEventItemWriter<Customer> writer = new StaxEventItemWriter<>();

        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String,Class> aliases = new HashMap<>();
        aliases.put("customer",Customer.class);
        marshaller.setAliases(aliases);

        writer.setRootTagName("customers");
        writer.setMarshaller(marshaller);

        String path ="f:\\1.xml";
        writer.setResource(new FileSystemResource(path));
        writer.afterPropertiesSet();
        return writer;
    }
}
