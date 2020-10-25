package com.example.springbatch.joboperator;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: fly
 * @create: 2020-10-25 21:25
 **/
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
