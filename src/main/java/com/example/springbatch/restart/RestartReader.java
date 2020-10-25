package com.example.springbatch.restart;

import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

/**
 * @author: fly
 * @create: 2020-10-25 08:28
 **/
@Component("restartReader")
public class RestartReader implements ItemStreamReader<Customer> {
    private FlatFileItemReader<Customer> customerFlatFileItemReader = new FlatFileItemReader<>();
    private Long curLine = 0L;
    private boolean restart = false;
    private ExecutionContext executionContext;

    public RestartReader(){
        customerFlatFileItemReader.setResource(new ClassPathResource("restart.txt"));
        //如何解析数据
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        //制定四个表头字段
        tokenizer.setNames(new String[]{"id","firstName","lastName","birthday"});
        //把一行映射为Customer对象
        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<>();
        mapper.setLineTokenizer(tokenizer);
        mapper.setFieldSetMapper(new FieldSetMapper<Customer>() {
            @Override
            public Customer mapFieldSet(FieldSet fieldSet) throws BindException {
                Customer customer = new Customer();
                customer.setId(fieldSet.readLong("id"));
                customer.setFirstName(fieldSet.readString("firstName"));
                customer.setLastName(fieldSet.readString("lastName"));
                customer.setBirthday(fieldSet.readString("birthday"));
                return customer;
            }
        });
        mapper.afterPropertiesSet();
        customerFlatFileItemReader.setLineMapper(mapper);

    }

    @Override
    public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Customer customer = null;
        this.curLine++;
        if (restart){
            customerFlatFileItemReader.setLinesToSkip(this.curLine.intValue()-1);
            restart = false;
            System.out.println("Start reading from line: "+ this.curLine);
        }
        customerFlatFileItemReader.open(this.executionContext);
        customer = customerFlatFileItemReader.read();
        if (customer != null && customer.getFirstName().equals("WrongName")){
            throw new RuntimeException("Something wrong.Customer id: "+customer.getId());
        }
        return customer;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.executionContext = executionContext;
        if (executionContext.containsKey("curLine")){
            this.curLine = executionContext.getLong("curLine");
            this.restart = true;
        }else{
            this.curLine = 0L;
            executionContext.put("curLine",this.curLine);
            System.out.println("Start reading from line: "+this.curLine +1);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put("curLine",this.curLine);
        System.out.println("currentLine:"+this.curLine);
    }

    @Override
    public void close() throws ItemStreamException {

    }
}
