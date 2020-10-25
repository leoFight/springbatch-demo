# 第一章 SpringBatch入门

## 第一节 SpringBatch概述

Spring Batch是一个轻量级的、完善的批处理框架，旨在帮助企业建立健壮、高效的批处理应用。Spring Batch是Spring的一个子项目，使用Java语言并基于Spring框架为基础开发，使得已经使用Spring框架的开发者或者企业更容易访问和利用企业服务。

Spring Batch提供了大量可重用的组件，包括日志、追踪、事务、任务作业统计、任务重启、跳过、重复、资源管理。对于大数据量和高性能的批处理任务，Spring Batch同样提供了高级功能和特性来支持，比如分区功能、远程功能。总之，通过Spring Batch能够支持简单的、复杂的和大数据量的批处理作业。

Spring Batch是一个批处理应用框架，不是调度框架，但需要和调度框架合作来构建完成的批处理任务。它只关注批处理任务相关的问题，如事务、并发、监控、执行等，并不提供相应的调度功能。如果需要使用调度框架，在商业软件和开源软件中已经有很多优秀的企业级调度框架（如Quartz、Tivoli、Control-M、Cron等）可以使用。

框架主要有以下功能：

Transaciton management(事务管理)

Chunk based processing(基于块的处理)

Declarative I/O(声明式的输入输出)

Start/Stop/Restart(启动/停止/再启动)

Retry/Skip(重试/跳过)

Web based administration interface ([Spring Cloud Data Flow](https://cloud.spring.io/spring-cloud-dataflow))（基于web的管理界面）






框架一共有4个主要角色：JobLauncher是任务启动器，通过它来启动任务，可以看做是程序的入口。Job代表这一个具体的任务。Step代表这一个具体的步骤，一个Job可以包含多个Step（想象把大象放进冰箱这个任务需要多少个步骤你就明白了）。JobRepository是存储数据的地方，可以看做是一个数据库的接口，在任务执行的时候需要通过它来记录任务状态等等信息。



## 第二节 搭建Spring Batch项目

`https://start.spring.io/`

~~~xml
<dependency>
   <groupId>com.h2database</groupId>
   <artifactId>h2</artifactId>
   <scope>runtime</scope>
</dependency>
~~~



## 第三节 SpringBatch入门程序

~~~java
@Configuration
@EnableBatchProcessing
public class JobConfiguartion {

    //注入创建任务对象的对象
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    //任务的执行由Step决定
    //注入创建Step对象的对象
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    //创建任务对象
    @Bean
    public Job helloWorldJob(){
        return jobBuilderFactory.get("helloWorldJob")
                .start(step1())
                .build();
    }
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Hello World");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
}

~~~

## 第四节 替换为MYSQL数据库

~~~xml
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
~~~

~~~properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://192.168.1.6:3306/springbatch?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.schema=classpath:org/springframework/batch/core/schema-mysql.sql
spring.batch.initialize-schema=always

~~~

## 第五节 核心API


~~~markdown
1. JobInstance：该领域概念和Job的关系与Java中实例和类的关系一样，Job定义了一个工作流程。JobInstance就是该工作流程的一个具体实例。一个Job可以由多个JobInstance，多个JobInstance之间的区分就要靠另外一个领域概念JobParameters了。

2. JobParameters：是一组可以贯穿整个Job的运行时配置参数。不同的配置将产生不同的JobInstance。如果你是使用相同的JobParameters运行同一个Job。那个这次运行会重用上一次创建的JobInstance。

3. JobExecution：该领域概念表示JobInstance的一次运行，JobInstance运行时可能会成功或者失败。每一次JobInstance的运行都会产生一个JobExecution。同一个JobInstance（JobParameters相同）可以多次运行。

4. StepExecution：类似于JobExecution,该领域对象表示Step的一次运行。Step是Job的一部分，因此一个StepExecution会关联到一个Jobexecution。另外，该对象还会存储很多与该次Step运行相关的所有数据。因此该对象也有很多属性，并且需要持久化支持一些Spring Batch的特性。

5. ExecutionContext：从前面的JobExecution，StepExecution的属性介绍中已经提到了该领域概念。说穿了，该领域概念就是一个容器，该容器由Batch框架控制，框架会对该容器持久化，开发人员可以使用该容器保存一下数据，已支持在整个BatchJob或者整个Step中共享这些数据。
~~~

# 第二章 作业流

## 第一节 Job创建和使用

Job：作业。批处理中的核心概念，是Batch操作的基础单元。

每个作业Job有一个或者多个作业步骤Step；

~~~java
@Configuration
@EnableBatchProcessing
public class JobDemo {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job JobDemoJob() {
        return jobBuilderFactory.get("JobDemoJob")
                //.start(step1()).next(step2()).next(step3())
                .start(step1()).on("COMPLETED").to(step2())
                .from(step2()).on("COMPLETED").to(step3()).end().build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("step3");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("step2");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("step1");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }
}

~~~

## 第二节 Flow的创建和使用

1. Flow是多个Step的集合
2. 可以被多个Job复用
3. 使用FlowBuilder来创建

~~~java
@Configuration
@EnableBatchProcessing
public class FlowDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step flowDemoStep1(){
        return stepBuilderFactory.get("flowDemoStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("flowDemoStep1");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Bean
    public Step flowDemoStep2(){
        return stepBuilderFactory.get("flowDemoStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("flowDemoStep2");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Bean
    public Step flowDemoStep3(){
        return stepBuilderFactory.get("flowDemoStep3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("flowDemoStep3");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    //创建Flow对象，指明Flow对象包含哪些Step
    @Bean
    public Flow flowDemoFlow(){
        return new FlowBuilder<Flow>("flowDemoFlow").start(flowDemoStep1())
                .next(flowDemoStep2()).build();
    }

    //创建Job对象
    @Bean
    public Job flowDemoJob(){
        return jobBuilderFactory.get("flowDemoJob").start(flowDemoFlow()).next(flowDemoStep3()).end().build();
    }
}

~~~

## 第三节 split实现并发执行

实现任务中的多个step或多个flow并发执行

1. 创建若干个step
2. 创建两个flow
3. 创建一个任务包含以上两个flow，并让这两个flow并发执行



~~~java

@Configuration
@EnableBatchProcessing
public class SplitDemo {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step splitDemoStep1(){
        return stepBuilderFactory.get("splitDemoStep1").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("splitDemoStep1");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step splitDemoStep2(){
        return stepBuilderFactory.get("splitDemoStep2").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("splitDemoStep2");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step splitDemoStep3(){
        return stepBuilderFactory.get("splitDemoStep3").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("splitDemoStep3");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Flow splitDemoFlow1(){
        return new FlowBuilder<Flow>("splitDemoFlow1").start(splitDemoStep1()).build();
    }
    @Bean
    public Flow splitDemoFlow2(){
        return new FlowBuilder<Flow>("splitDemoFlow2").start(splitDemoStep2()).next(splitDemoStep3()).build();
    }
    @Bean
    public Job splitDemoJob(){
        return jobBuilderFactory.get("splitDemoJob").start(splitDemoFlow1())
                .split(new SimpleAsyncTaskExecutor()).add(splitDemoFlow2()).end().build();
    }
}

~~~

## 第四节 决策器的使用

接口：JobExecutionDecider

~~~java
@Configuration
@EnableBatchProcessing
public class DeciderDemo {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step deciderDemoStep1(){
        return  stepBuilderFactory.get("deciderDemoStep1").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("deciderDemoStep1");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step deciderDemoStep2(){
        return  stepBuilderFactory.get("deciderDemoStep2").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("deciderDemoStep2");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step deciderDemoStep3(){
        return  stepBuilderFactory.get("deciderDemoStep3").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("deciderDemoStep3");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public JobExecutionDecider myDecider(){
        return new MyDecider();
    }

    @Bean
    public Job deciderDemoJob(){
        return jobBuilderFactory.get("deciderDemoJob")
                .start(deciderDemoStep1()).next(myDecider()).from(myDecider()).on("even")
                .to(deciderDemoStep2()).from(myDecider()).on("odd").to(deciderDemoStep3())
                .from(deciderDemoStep3()).on("*").to(myDecider())
                .end().build();
    }
}

~~~

~~~java
public class MyDecider implements JobExecutionDecider {

    private int count;
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        count++;
        if (count%2 == 0){
            return new FlowExecutionStatus("even");
        }else {
            return new FlowExecutionStatus("odd");
        }
    }
}

~~~

## 第五节 Job的嵌套

一个Job可以嵌套在另一个Job中，被嵌套的Job称为子Job，外部Job称为父Job。子Job不能单独执行，需要由父Job来启动



案例：创建两个Job，作为子job，再创建一个Job作为父Job



~~~java

@Configuration
@EnableBatchProcessing
public class ChildJob1 {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step childJob1Step1(){
        return stepBuilderFactory.get("childJob1Step1").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("childJobStep1");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Job childJobOne(){
        return jobBuilderFactory.get("childJobOne").start(childJob1Step1()).build();
    }
}

~~~

~~~java

@Configuration
@EnableBatchProcessing
public class ChildJob2 {
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Bean
    public Step childJob2Step1(){
        return stepBuilderFactory.get("childJob2Step1").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("childJob2Step1");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }
    @Bean
    public Step childJob2Step2(){
        return stepBuilderFactory.get("childJob2Step2").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("childJob2Step2");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }
    
    @Bean
    public Job childJobTwo(){
        return jobBuilderFactory.get("childJobTwo").start(childJob2Step1()).next(childJob2Step2()).build();
    }
}

~~~

~~~java
@Configuration
@EnableBatchProcessing
public class NestedDemo {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private Job childJobOne;
    @Autowired
    private Job childJobTwo;
    @Autowired
    private JobLauncher jobLauncher;

    @Bean
    public Job parentJob(JobRepository repository, PlatformTransactionManager transactionManager){
        return jobBuilderFactory.get("parentJob").start(childJob1(repository,transactionManager)).next(childJob2(repository,transactionManager)).build();
    }

     private Step childJob2(JobRepository repository, PlatformTransactionManager transactionManager) {
        return new JobStepBuilder(new StepBuilder("childJob2")).job(childJobTwo)
                .launcher(jobLauncher).repository(repository)
                .transactionManager(transactionManager).build();
    }
    private Step childJob1(JobRepository repository, PlatformTransactionManager transactionManager) {
        return new JobStepBuilder(new StepBuilder("childJob1")).job(childJobOne)
                .launcher(jobLauncher).repository(repository)
                .transactionManager(transactionManager).build();
    }
}

~~~

~~~properties
spring.batch.job.names=parentJob
~~~



## 第六节 监听器的使用

用来监听批处理作用的执行情况

创建监听可以通过实现接口或使用注解

`JobExecutionListener(before,after)`

`StepExecutionListener(before,after)`

`ChunkListener(before,after,error)`

`ItemReadlistener,ItemProcessListener,ItemWriteListener(before,after,error)`



~~~java
public class MyChunkListener {

    @BeforeChunk
    public void beforeChunk(ChunkContext chunkContext){
        System.out.println(chunkContext.getStepContext().getStepExecution()+"before ...");
    }
    @AfterChunk
    public void afterChunk(ChunkContext chunkContext){
        System.out.println(chunkContext.getStepContext().getStepExecution()+"after ...");
    }
}
~~~

~~~java
public class MyJobListener implements JobExecutionListener
{
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println(jobExecution.getJobInstance().getJobName()+"before....");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println(jobExecution.getJobInstance().getJobName()+"after....");
    }
}
~~~

~~~java
@Configuration
@EnableBatchProcessing
public class ListenerDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job listenerJob(){
        return jobBuilderFactory.get("listenerJob")
                .start(step1()).listener(new MyJobListener()).build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<String,String>chunk(2)//read,process,write
                .faultTolerant().listener(new MyChunkListener())
                .reader(read())
                .writer(write())
                .build();
    }
    @Bean
    public ItemWriter<String> write() {
        return new ItemWriter<String>() {
            @Override
            public void write(List<? extends String> list) throws Exception {
                 for (String item:list){
                     System.out.println(item);
                 }
            }
        };
    }
    @Bean
    public ItemReader<String> read() {
        return new ListItemReader<>(Arrays.asList("java","spring","mybatis"));
    }
}
~~~



## 第七节 Job参数

在Job运行时可以以key=value形式传递参数

~~~java
@Configuration
@EnableBatchProcessing
public class ParametersDemo implements StepExecutionListener {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private Map<String,JobParameter> parameters;
    @Bean
    public Job parameterJob(){
        return jobBuilderFactory.get("parameterJob").start(parameterStep()).build();
    }

    //Job执行的step，Job使用的数据肯定是在step中使用
    //那我们只需要给step传递数据，如何给step传递参数？
    //使用监听，使用Step级别的监听来传递数据
    @Bean
    public Step parameterStep() {
        return stepBuilderFactory.get("parameterStep")
                .listener(this)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        //接收到的参数的值
                        System.out.println(parameters.get("info"));
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        parameters = stepExecution.getJobParameters().getParameters();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
~~~



#  第三章 数据输入

## 第一节 ItemReader概述

~~~java

@Configuration
@EnableBatchProcessing
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

~~~

~~~java

public class MyReader implements ItemReader<String> {
    private final Iterator<String> iterator;

    public MyReader(List<String> data) {
        this.iterator = data.iterator();
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (iterator.hasNext()) {
            return this.iterator.next();
        } else {
            return null;
        }
    }

}
~~~



## 第二节 从数据库中读取数据

~~~sql
create table USER
(
    ID       int primary key auto_increment,
    username varchar(30),
    password varchar(20),
    age      int
)
~~~

~~~JAVA
public class User {
    private Integer id;
    private String username;
    private String password;
    private int age;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                '}';
    }
}

~~~

~~~JAVA
@Component("dbJdbcWriter")
public class DbJdbcWriter implements ItemWriter<User> {

    @Override
    public void write(List<? extends User> list) throws Exception {
        for (User user:list){
            System.out.println(user.toString());
        }
    }
}
~~~

~~~JAVA
@Configuration
@EnableBatchProcessing
public class ItemReaderDbDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private DataSource dataSource;
    @Autowired
    @Qualifier("dbJdbcWriter")
    private ItemWriter<? super User> dbJdbcWriter;

    @Bean
    public Job itemReaderDbJob(){
        return jobBuilderFactory.get("itemReaderDbJob").start(itemReaderDbStep()).build();
    }

    private Step itemReaderDbStep() {
        return stepBuilderFactory.get("itemReaderDbStep")
                .<User,User>chunk(2)
                .reader(dbJdbcReader())
                .writer(dbJdbcWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<User>  dbJdbcReader() {
        JdbcPagingItemReader<User> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(2);
        //把读取到的记录转换成User对象
        reader.setRowMapper(new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                User user = new User();
                user.setId(resultSet.getInt(1));
                user.setUsername(resultSet.getString(2));
                user.setPassword(resultSet.getString(3));
                user.setAge(resultSet.getInt(4));
                return user;
            }
        });
        //指定sql语句
        MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
        provider.setSelectClause("id,username,password,age");
        provider.setFromClause("from USER");

        //指定根据那个字段进行排序
        Map<String, Order> sort = new HashMap<>(1);
        sort.put("id",Order.ASCENDING);
        provider.setSortKeys(sort);
        reader.setQueryProvider(provider);
        return reader;
    }
}

~~~

## 第三节 从普通文件中读取数据

~~~markdown
id,firstName,lastName,birthday
1,Stone,Barrett,1964-10-19 14:11:03
2,Raymond,Pace,1977-12-11 21:44:30
3,Armando,Logan,1986-12-25 11:54:28
4,Latifah,Barnett,1959-07-24 06:00:16
5,Cassandra,Moses,1956-09-14 06:49:28
6,Audra,Hopkins,1984-08-30 04:18:10
~~~

~~~java
public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String birthday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}

~~~

~~~java
@Component("flatFileWriter")
public class FlatFileWriter implements ItemWriter<Customer> {
    @Override
    public void write(List<? extends Customer> list) throws Exception {
        for (Customer customer :list){
            System.out.println(customer.toString());
        }
    }
}
~~~

~~~java
@Configuration
@EnableBatchProcessing
public class FileItemReaderDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("flatFileWriter")
    private ItemWriter<? super Customer> flatFileWriter;

    @Bean
    public Job FileItemReaderDemoJob(){
        return jobBuilderFactory.get("FileItemReaderDemoJob").start(FileItemReaderDemoStep()).build();
    }

    @Bean
    public Step FileItemReaderDemoStep() {
        return stepBuilderFactory.get("FileItemReaderDemoStep")
                .<Customer,Customer>chunk(1)
                .reader(flatFileReader())
                .writer(flatFileWriter)
                .build();
    }
    @Bean
    public FlatFileItemReader<Customer> flatFileReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<Customer>();
        reader.setResource(new ClassPathResource("customer.txt"));
        reader.setLinesToSkip(1);//跳过第一行

        //解析数据
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"id","firstName","lastName","birthday"});
        //把解析出的一行数据映射为Customer对象
        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<Customer>();
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
        reader.setLineMapper(mapper);
        return  reader;
    }
}

~~~

## 第四节 从XML文件中读取数据

~~~XML
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-oxm</artifactId>
</dependency>
<dependency>
  <groupId>com.thoughtworks.xstream</groupId>
  <artifactId>xstream</artifactId>
  <version>1.4.11.1</version>
</dependency>
~~~

~~~xml
<?xml version="1.0" encoding="UTF-8" ?>
<customers>
    <customer>
        <id>1</id>
        <firstName>Mufutan</firstName>
        <lastName>Maddox</lastName>
        <birthday>2017-06-05 19:43:51PM</birthday>
    </customer>
    <customer>
        <id>2</id>
        <firstName>Brenden</firstName>
        <lastName>Cobb</lastName>
        <birthday>2017-01-06 13:18:17PM</birthday>
    </customer>
    <customer>
        <id>3</id>
        <firstName>Kerry</firstName>
        <lastName>Joseph</lastName>
        <birthday>2016-09-15 18:32:33PM</birthday>
    </customer>
</customers>
~~~

~~~java
@Component("xmlFileWrite")
public class XmlFileWriter implements ItemWriter<Customer> {

    @Override
    public void write(List<? extends Customer> list) throws Exception {
        for (Customer customer : list){
            System.out.println(customer.toString());
        }
    }
}
~~~

~~~java
@Configuration
@EnableBatchProcessing
public class XmlItemReaderDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("xmlFileWrite")
    private ItemWriter<? super Customer> xmlFileWrite;

    @Bean
    public Job xmlItemReaderDemoJob(){
        return jobBuilderFactory.get("xmlItemReaderDemoJob")
                .start(xmlItemReaderDemoStep())
                .build();
    }

    @Bean
    public Step xmlItemReaderDemoStep() {
        return stepBuilderFactory.get("xmlItemReaderDemoStep")
                .<Customer,Customer>chunk(1)
                .reader(xmlFileReader())
                .writer(xmlFileWrite)
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemReader<Customer> xmlFileReader() {
        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();
        reader.setResource(new ClassPathResource("customer.xml"));

        //指定需要处理的根标签
        reader.setFragmentRootElementName("customer");
        //把xml转成对象
        XStreamMarshaller unmarshaller = new XStreamMarshaller();
        Map<String,Class> map = new HashMap<>();
        map.put("customer",Customer.class);
        unmarshaller.setAliases(map);

        reader.setUnmarshaller(unmarshaller);
        return reader;
    }
}

~~~



## 第五节 从多文件中读取数据

~~~markdown
1,Stone1,Barrett,1964-10-19 14:11:03
2,Raymond1,Pace,1977-12-11 21:44:30
3,Armando1,Logan,1986-12-25 11:54:28
4,Latifah1,Barnett,1959-07-24 06:00:16
5,Cassandra1,Moses,1956-09-14 06:49:28
6,Audra1,Hopkins,1984-08-30 04:18:10
~~~

~~~markdown
1,Stone2,Barrett,1964-10-19 14:11:03
2,Raymond2,Pace,1977-12-11 21:44:30
3,Armando2,Logan,1986-12-25 11:54:28
4,Latifah2,Barnett,1959-07-24 06:00:16
5,Cassandra2,Moses,1956-09-14 06:49:28
6,Audra2,Hopkins,1984-08-30 04:18:10
~~~

~~~markdown
1,Stone3,Barrett,1964-10-19 14:11:03
2,Raymond3,Pace,1977-12-11 21:44:30
3,Armando3,Logan,1986-12-25 11:54:28
4,Latifah3,Barnett,1959-07-24 06:00:16
5,Cassandra3,Moses,1956-09-14 06:49:28
6,Audra3,Hopkins,1984-08-30 04:18:10
~~~

~~~java
public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String birthday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}

~~~

~~~java
@Component("multiFileWriter")
public class MultiFileWriter implements ItemWriter<Customer> {
    @Override
    public void write(List<? extends Customer> list) throws Exception {
        for (Customer customer : list){
            System.out.println(customer.toString());
        }
    }
}
~~~

~~~java

@Configuration
@EnableBatchProcessing
public class MultiFileItemReaderDemo {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Value("classpath:/file*.txt")
    private Resource[] fileResources;
    @Autowired
    @Qualifier("multiFileWriter")
    private ItemWriter<? super Customer> multiFileWriter;

    @Bean
    public Job multiFileItemReaderDemoJob(){
        return jobBuilderFactory.get("multiFileItemReaderDemoJob")
                .start(multiFileItemReaderDemoStep())
                .build();
    }

    @Bean
    public Step multiFileItemReaderDemoStep() {
        return stepBuilderFactory.get("multiFileItemReaderDemoStep")
                .<Customer,Customer>chunk(1)
                .reader(multiFileReader())
                .writer(multiFileWriter)
                .build();
    }
    @Bean
    @StepScope
    public MultiResourceItemReader<Customer> multiFileReader() {
        MultiResourceItemReader<Customer> reader = new MultiResourceItemReader<>();
        reader.setDelegate(flatFileReader());
        reader.setResources(fileResources);
        return reader;
    }

    @Bean
    public ResourceAwareItemReaderItemStream<? extends Customer> flatFileReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<Customer>();
        //reader.setResource(new ClassPathResource("customer.txt"));

        //解析数据
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"id","firstName","lastName","birthday"});
        //把解析出的一行数据映射为Customer对象
        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<Customer>();
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
        reader.setLineMapper(mapper);
        return  reader;
    }
}

~~~

## 第六节 ItemReader异常处理及重启

~~~markdown
1,Stone1,Barrett,1964-10-19 14:11:03
2,Raymond1,Pace,1977-12-11 21:44:30
3,Armando1,Logan,1986-12-25 11:54:28
4,Latifah1,Barnett,1959-07-24 06:00:16
5,WrongName1,Moses,1956-09-14 06:49:28
6,Audra1,Hopkins,1984-08-30 04:18:10
~~~

~~~java

public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String birthday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}

~~~

~~~java
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

~~~

~~~java
@Component("restartWriter")
public class RestartWriter implements ItemWriter<Customer> {
    @Override
    public void write(List<? extends Customer> list) throws Exception {
        for (Customer customer : list){
            System.out.println(customer.toString());
        }
    }
}
~~~

~~~java
@Configuration
@EnableBatchProcessing
public class RestartDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("restartReader")
    private ItemReader<Customer> restartReader;
    @Autowired
    @Qualifier("restartWriter")
    private ItemWriter<? super Customer> restartWriter;

    @Bean
    public Job restartJob(){
        return jobBuilderFactory.get("restartsJob")
                .start(restartStep())
                .build();
    }
    @Bean
    public Step restartStep() {
        return stepBuilderFactory.get("restartStep")
                .<Customer,Customer>chunk(1)
                .reader(restartReader)
                .writer(restartWriter)
                .build();
    }
}

~~~



# 第四章 数据输出

## 第一节 ItemWriter概述

ItemReader是一个数据一个数据的读，而ItemWriter是一批一批的输出。

~~~java
@Component("myWriter")
public class MyWriter implements ItemWriter<String> {
    @Override
    public void write(List<? extends String> list) throws Exception {
        System.out.println(list.size());
        for (String str:list){
            System.out.println(str);
        }
    }
}
~~~

~~~java

@Configuration
@EnableBatchProcessing
public class ItemWriterDemo {


    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("myWriter")
    private ItemWriter<? super String> myWriter;

    @Bean
    public Job itemWriterDemoJob(){
        return jobBuilderFactory.get("itemWriterDemoJob").start(itemWriterDemoStep()).build();
    }

    @Bean
    public Step itemWriterDemoStep() {
        return stepBuilderFactory.get("itemWriterDemoStep").<String,String>chunk(5)
                .reader(myRead())
                .writer(myWriter)
                .build();

    }
    @Bean
    public ItemReader<? extends String> myRead() {
        List<String> items = new ArrayList<>();
        for(int i =1 ; i <= 50 ;i++){
            items.add("java"+i);
        }
        return new ListItemReader<String>(items);
    }
}

~~~



## 第二节 数据输出到数据库

- Neo4jItemWriter
- MongoItemWriter
- RepositoryItemWriter
- HibernateItemWriter
- JdbcBatchItemWriter
- JpaItemWriter
- GemfireItemWriter



~~~sql
create table CUSTOMER
(
    ID       bigint primary key ,
    firstName varchar(50),
    lastName varchar(50),
    birthday      varchar(50)
)
~~~

~~~markdown
id,firstName,lastName,birthday
1,Stone,Barrett,1964-10-19 14:11:03
2,Raymond,Pace,1977-12-11 21:44:30
3,Armando,Logan,1986-12-25 11:54:28
4,Latifah,Barnett,1959-07-24 06:00:16
5,Cassandra,Moses,1956-09-14 06:49:28
6,Audra,Hopkins,1984-08-30 04:18:10
~~~

~~~java

public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String birthday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}

~~~

~~~java

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

~~~

~~~java

@Configuration
public class FlatFileReaderConfig {
    @Bean
    public FlatFileItemReader<Customer> flatFileReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<Customer>();
        reader.setResource(new ClassPathResource("customer.txt"));
        reader.setLinesToSkip(1);//跳过第一行

        //解析数据
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"id","firstName","lastName","birthday"});
        //把解析出的一行数据映射为Customer对象
        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<Customer>();
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
        reader.setLineMapper(mapper);
        return  reader;
    }
}


~~~

~~~java

@Configuration
@EnableBatchProcessing
public class ItemWriterDbDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("flatFileReader")
    private ItemReader<Customer> flatFileReader;
    @Autowired
    @Qualifier("itemWriterDb")
    private ItemWriter<? super Customer> itemWriterDb;

    @Bean
    public Job ItemWriterDbDemoJob(){
        return jobBuilderFactory.get("ItemWriterDbDemoJob")
                .start(ItemWriterDbDemoStep())
                .build();
    }

    @Bean
    public Step ItemWriterDbDemoStep() {
        return stepBuilderFactory.get("ItemWriterDbDemoStep")
                .<Customer,Customer>chunk(2)
                .reader(flatFileReader)
                .writer(itemWriterDb)
                .build();
    }
}

~~~

## 第三节 数据输出到普通文件

FlatFileItemWriter

案例，从数据库读取数据写入到文件

~~~java

public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String birthday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}
~~~

~~~java
@Configuration
public class DbJdbcReaderConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcPagingItemReader<Customer> dbJdbcReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(2);
        //把读取到的记录转换成Customer对象
        reader.setRowMapper(new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                Customer user = new Customer();
                user.setId(resultSet.getLong(1));
                user.setFirstName(resultSet.getString(2));
                user.setLastName(resultSet.getString(3));
                user.setBirthday(resultSet.getString(4));
                return user;
            }
        });
        //指定sql语句
        MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
        provider.setSelectClause("id,firstName,lastName,birthday");
        provider.setFromClause("from CUSTOMER");

        //指定根据那个字段进行排序
        Map<String, Order> sort = new HashMap<>(1);
        sort.put("id",Order.ASCENDING);
        provider.setSortKeys(sort);
        reader.setQueryProvider(provider);
        return reader;
    }
}

~~~

~~~java
@Configuration
public class FileItemWriterConfig {
    @Bean
    public FlatFileItemWriter<Customer> fileItemWriter() throws Exception {

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
}
~~~



~~~java
@Configuration
@EnableBatchProcessing
public class FileItemWriterDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("dbJdbcReader")
    private ItemReader<? extends Customer> dbJdbcReader;
    @Autowired
    @Qualifier("fileItemWriter")
    private ItemWriter<? super Customer> fileItemWriter;

    @Bean
    public Job fileItemWriterDemoJob(){
        return jobBuilderFactory.get("fileItemWriterDemoJob")
                .start(fileItemWriterDemoStep())
                .build();
    }

    @Bean
    public Step fileItemWriterDemoStep() {
        return stepBuilderFactory.get("fileItemWriterDemoStep")
                .<Customer,Customer>chunk(2)
                .reader(dbJdbcReader)
                .writer(fileItemWriter)
                .build();
    }
}

~~~



## 第四节 数据输出到xml文件

StaxEvenItemWriter

~~~java

public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String birthday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}
~~~

~~~java
@Configuration
public class DbJdbcReaderConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcPagingItemReader<Customer> dbJdbcReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(2);
        //把读取到的记录转换成Customer对象
        reader.setRowMapper(new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                Customer user = new Customer();
                user.setId(resultSet.getLong(1));
                user.setFirstName(resultSet.getString(2));
                user.setLastName(resultSet.getString(3));
                user.setBirthday(resultSet.getString(4));
                return user;
            }
        });
        //指定sql语句
        MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
        provider.setSelectClause("id,firstName,lastName,birthday");
        provider.setFromClause("from CUSTOMER");

        //指定根据那个字段进行排序
        Map<String, Order> sort = new HashMap<>(1);
        sort.put("id",Order.ASCENDING);
        provider.setSortKeys(sort);
        reader.setQueryProvider(provider);
        return reader;
    }
}

~~~

~~~java
@Configuration
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

~~~

~~~java

@Configuration
@EnableBatchProcessing
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

~~~

## 第五节 数据输出到多个文件

CompositeItemWriter

ClassifierCompositeItemWriter



~~~java
public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String birthday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}
~~~

~~~java
@Configuration
public class DbJdbcReaderConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcPagingItemReader<Customer> dbJdbcReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(2);
        //把读取到的记录转换成Customer对象
        reader.setRowMapper(new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                Customer user = new Customer();
                user.setId(resultSet.getLong(1));
                user.setFirstName(resultSet.getString(2));
                user.setLastName(resultSet.getString(3));
                user.setBirthday(resultSet.getString(4));
                return user;
            }
        });
        //指定sql语句
        MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
        provider.setSelectClause("id,firstName,lastName,birthday");
        provider.setFromClause("from CUSTOMER");

        //指定根据那个字段进行排序
        Map<String, Order> sort = new HashMap<>(1);
        sort.put("id",Order.ASCENDING);
        provider.setSortKeys(sort);
        reader.setQueryProvider(provider);
        return reader;
    }
}

~~~

~~~java
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

~~~

~~~java
@Configuration
@EnableBatchProcessing
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
        return jobBuilderFactory.get("multiFileItemWriterDemoJob")
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

~~~

## 第六节 ItemProcessor的使用

ItemProcessor<I,O)用于业务逻辑判断，验证，过滤等功能

CompositelItemProcessor

案例：从数据库中读取数据，然后对数据进行处理，最后输出到普通文件

~~~java
public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String birthday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}
~~~

~~~java
@Configuration
public class DbFileWriterConfig {
    @Bean
    public FlatFileItemWriter<Customer> dbFileWrite() throws Exception {

        //把Customer对象转换成字符串输出到文件
        FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<>();
        //本地文件路径
        writer.setResource(new FileSystemResource("f:\\2.txt"));
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

}
~~~

~~~java
@Configuration
public class DbJdbcReaderConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcPagingItemReader<Customer> dbJdbcReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(2);
        //把读取到的记录转换成Customer对象
        reader.setRowMapper(new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                Customer user = new Customer();
                user.setId(resultSet.getLong(1));
                user.setFirstName(resultSet.getString(2));
                user.setLastName(resultSet.getString(3));
                user.setBirthday(resultSet.getString(4));
                return user;
            }
        });
        //指定sql语句
        MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
        provider.setSelectClause("id,firstName,lastName,birthday");
        provider.setFromClause("from CUSTOMER");

        //指定根据那个字段进行排序
        Map<String, Order> sort = new HashMap<>(1);
        sort.put("id",Order.ASCENDING);
        provider.setSortKeys(sort);
        reader.setQueryProvider(provider);
        return reader;
    }
}
~~~

~~~java
@Component
public class FirstNameUpperProcessor  implements ItemProcessor<Customer,Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {
        Customer customer1 = new Customer();
        customer1.setId(customer.getId());
        customer1.setFirstName(customer.getFirstName().toUpperCase());
        customer1.setLastName(customer.getLastName());
        customer1.setBirthday(customer.getBirthday());
        return customer1;
    }
}
~~~

~~~java
@Component
public class IdFilterProcessor implements ItemProcessor<Customer,Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {
        if (customer.getId() % 2 ==0 ){
            return customer;
        }else {
            return null;
        }
    }
}
~~~

~~~java
@Configuration
@EnableBatchProcessing
public class ItemProcessorDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("dbJdbcReader")
    private ItemReader<? extends Customer> dbJdbcReader;
    @Autowired
    @Qualifier("dbFileWrite")
    private ItemWriter<? super Customer> dbFileWrite;
    @Autowired
    @Qualifier("firstNameUpperProcessor")
    private ItemProcessor<Customer,Customer> firstNameUpperProcessor;
    @Autowired
    @Qualifier("idFilterProcessor")
    private ItemProcessor<Customer,Customer> idFilterProcessor;

    @Bean
    public Job itemProcessorDemoJob(){
        return jobBuilderFactory.get("itemProcessorDemoJob")
                .start(itemProcessorDemoStep())
                .build();
    }

    @Bean
    public Step itemProcessorDemoStep() {
        return stepBuilderFactory.get("itemProcessorDemoStep")
                .<Customer,Customer>chunk(2)
                .reader(dbJdbcReader)
                //.processor(firstNameUpperProcessor)
                .processor(process())
                .writer(dbFileWrite)
                .build();
    }

    //多种数据的处理方式
    @Bean
    public CompositeItemProcessor<Customer,Customer> process(){
        CompositeItemProcessor<Customer, Customer> processor = new CompositeItemProcessor<>();
        List<ItemProcessor<Customer,Customer>> delagates = new ArrayList<>();
        delagates.add(firstNameUpperProcessor);
        delagates.add(idFilterProcessor);
        processor.setDelegates(delagates);
        return processor;
    }
}
~~~



# 第五章 错误处理

## 第一节 错误处理概述

默认情况下当任务出现异常时，SpringBatch会结束任务，当使用相同的参数重启任务时，SpringBatch回去执行未执行的剩余任务

~~~java

@Configuration
@EnableBatchProcessing
public class ErrorDemo {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job errorDemoJob(){
        return jobBuilderFactory.get("errorDemoJob")
                .start(errorStep1())
                .next(errorStep2())
                .build();
    }
    @Bean
    public Step errorStep1() {
        return stepBuilderFactory.get("errorStep1")
                .tasklet(errorHanding())
                .build();
    }
    @Bean
    public Step errorStep2() {
        return stepBuilderFactory.get("errorStep2")
                .tasklet(errorHanding())
                .build();
    }

    private Tasklet errorHanding() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                Map<String,Object> stepExecutionContext = chunkContext.getStepContext().getStepExecutionContext();

                if (stepExecutionContext.containsKey("example")){
                    System.out.println("The second run will success");
                    return RepeatStatus.FINISHED;
                }else{
                    System.out.println("The first run  will fail");
                    chunkContext.getStepContext().getStepExecution().getExecutionContext().put("example",true);
                    throw new RuntimeException("error...");
                }
            }
        };
    }


}
~~~

## 第二节 错误重试（Retry）

~~~java
@Component
public class RetryItemWriter implements ItemWriter<String> {
    @Override
    public void write(List<? extends String> list) throws Exception {
        for (String item: list){
            System.out.println(item);
        }
    }
}
~~~

~~~java
@Component
public class RetryItemProcessor implements ItemProcessor<String,String> {
    private int attemptCount = 0;

    @Override
    public String process(String s) throws Exception {
        System.out.println("processing s "+s);
        if (s.equalsIgnoreCase("26")){
            attemptCount++;
            if (attemptCount >= 3){
                System.out.println("Retried "+attemptCount+ " times success.");
                return String.valueOf(Integer.valueOf(s) * -1);
            }else{
                System.out.println("Processed the "+attemptCount+ " times fail");
                throw new CustomRetryExcetion("Process failed. Attempt："+attemptCount);
            }
        }else{
            return String.valueOf(Integer.valueOf(s) * -1);
        }
    }
}
~~~

~~~java
public class CustomRetryExcetion extends Exception {
    public CustomRetryExcetion() {
        super();
    }
    public CustomRetryExcetion(String msg){
        super(msg);
    }
}

~~~

~~~java
@Configuration
@EnableBatchProcessing
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
~~~



## 第三节 错误跳过（Skip）

~~~java
public class CustomSkipExcetion extends Exception {
    public CustomSkipExcetion() {
        super();
    }
    public CustomSkipExcetion(String msg){
        super(msg);
    }
}

~~~

~~~java
@Component
public class SkipItemProcessor implements ItemProcessor<String,String> {
    private int attemptCount = 0;

    @Override
    public String process(String s) throws Exception {
        System.out.println("processing s "+s);
        if (s.equalsIgnoreCase("26")){
            attemptCount++;
            if (attemptCount >= 3){
                System.out.println("Retried "+attemptCount+ " times success.");
                return String.valueOf(Integer.valueOf(s) * -1);
            }else{
                System.out.println("Processed the "+attemptCount+ " times fail");
                throw new CustomSkipExcetion("Process failed. Attempt："+attemptCount);
            }
        }else{
            return String.valueOf(Integer.valueOf(s) * -1);
        }
    }
}
~~~

~~~java
@Component
public class SkipItemWriter implements ItemWriter<String> {
    @Override
    public void write(List<? extends String> list) throws Exception {
        for (String item: list){
            System.out.println(item);
        }
    }
}
~~~

~~~java
@Configuration
@EnableBatchProcessing
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

~~~



## 第四节 错误跳过监听器（Skip Listener）

~~~JAVA
public class CustomSkipExcetion extends Exception {
    public CustomSkipExcetion() {
        super();
    }
    public CustomSkipExcetion(String msg){
        super(msg);
    }
}
~~~

~~~JAVA
@Component
public class SkipListenerItemProcessor implements ItemProcessor<String,String> {
    private int attemptCount = 0;

    @Override
    public String process(String s) throws Exception {
        System.out.println("processing s "+s);
        if (s.equalsIgnoreCase("26")){
            attemptCount++;
            if (attemptCount >= 3){
                System.out.println("Retried "+attemptCount+ " times success.");
                return String.valueOf(Integer.valueOf(s) * -1);
            }else{
                System.out.println("Processed the "+attemptCount+ " times fail");
                throw new CustomSkipExcetion("Process failed. Attempt："+attemptCount);
            }
        }else{
            return String.valueOf(Integer.valueOf(s) * -1);
        }
    }
}
~~~

~~~java
@Component
public class SkipListenerItemWriter implements ItemWriter<String> {
    @Override
    public void write(List<? extends String> list) throws Exception {
        for (String item: list){
            System.out.println(item);
        }
    }
}
~~~

~~~java
@Component
public class MySkipListener implements SkipListener<String,String> {
    @Override
    public void onSkipInRead(Throwable throwable) {

    }

    @Override
    public void onSkipInWrite(String s, Throwable throwable) {

    }

    @Override
    public void onSkipInProcess(String s, Throwable throwable) {
        System.out.println(s +" occur exection "+ throwable);
    }
}

~~~

~~~java
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

~~~



# 第六章 作用调度

## 第一节 JobLauncher的使用

控制任务什么时候启动

~~~xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
~~~

~~~java
@Configuration
@EnableBatchProcessing
public class JobLauncherDemo implements StepExecutionListener {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private Map<String, JobParameter> parameters;

    @Bean
    public Job jobLauncherDemoJob(){
        return jobBuilderFactory.get("jobLauncherDemoJob")
                .start(jobLauncherDemoStep())
                .build();
    }
    @Bean
    public Step jobLauncherDemoStep() {
        return stepBuilderFactory.get("jobLauncherDemoStep")
                .listener(this)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println(parameters.get("msg").getValue());
                        return RepeatStatus.FINISHED;
                    }
                }).build();

    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        parameters = stepExecution.getJobParameters().getParameters();

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}

~~~

~~~java
@RestController
public class JobLauncherController {
    @Autowired
    private  JobLauncher jobLauncher;
    @Autowired
    private  Job jobLauncherDemoJob;
    @RequestMapping("/job/{msg}")
    public  String jobRun(@PathVariable String msg) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters parameters = new JobParametersBuilder().addString("msg",msg).toJobParameters();
        jobLauncher.run(jobLauncherDemoJob,parameters);
        return "job success";
    }
}

~~~

启动后，访问：http://localhost:8080/job/hello



## 第二节 JobOperator的使用

~~~java
@Configuration
@EnableBatchProcessing
public class JobOperatorDemo implements StepExecutionListener , ApplicationContextAware {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private Map<String, JobParameter> parameters;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobExplorer jobExplorer;
    @Autowired
    private JobRegistry jobRegistry;

    private ApplicationContext context;

    @Bean
    public JobRegistryBeanPostProcessor jobRegistrar()throws Exception{
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();

        postProcessor.setJobRegistry(jobRegistry);
        postProcessor.setBeanFactory(context.getAutowireCapableBeanFactory());
        postProcessor.afterPropertiesSet();

        return postProcessor;
    }

    @Bean
    public JobOperator jobOperator(){
        SimpleJobOperator operator = new SimpleJobOperator();

        operator.setJobLauncher(jobLauncher);
        operator.setJobParametersConverter(new DefaultJobParametersConverter());
        operator.setJobRepository(jobRepository);
        operator.setJobExplorer(jobExplorer);
        operator.setJobRegistry(jobRegistry);

        return operator;
    }
    @Bean
    public Job jobOperatorDemoJob(){
        return jobBuilderFactory.get("jobOperatorDemoJob")
                .start(jobOperatorDemoStep())
                .build();
    }
    @Bean
    public Step jobOperatorDemoStep() {
        return stepBuilderFactory.get("jobOperatorDemoStep")
                .listener(this)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println(parameters.get("msg").getValue());
                        return RepeatStatus.FINISHED;
                    }
                }).build();

    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        parameters = stepExecution.getJobParameters().getParameters();

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}

~~~

~~~java
@RestController
public class JobOperatorController {
    @Autowired
    private JobOperator jobOperator;
    @RequestMapping("/job2/{msg}")
    public  String jobRun(@PathVariable String msg) throws JobParametersInvalidException, JobInstanceAlreadyExistsException, NoSuchJobException {
        //启动任务，同时传参数
        jobOperator.start("jobOperatorDemoJob","msg="+msg);
        return "job success";
    }
}

~~~

访问：http://localhost:8080/job2/hello