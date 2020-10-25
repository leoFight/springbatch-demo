package com.example.springbatch.joblauncher;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: fly
 * @create: 2020-10-25 21:25
 **/
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
