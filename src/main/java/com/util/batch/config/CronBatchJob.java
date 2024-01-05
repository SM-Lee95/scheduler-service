package com.util.batch.config;

import com.util.batch.service.BatchService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
@Slf4j
public class CronBatchJob extends QuartzJobBean {
    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String jobUrl = jobDataMap.getString("url");
        JobKey jobKey = context.getJobDetail().getKey();
        log.info("CronJob started :: jobKey : {}", jobKey);
        ApplicationContext applicationContext = (ApplicationContext)context.getScheduler().getContext().get("applicationContext");
        BatchService batchService = applicationContext.getBean(BatchService.class);
        batchService.callBatchURL(jobKey, jobUrl, "N");
        log.info("CronJob finished ::::::::: ");
    }
}
