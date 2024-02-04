package com.util.batch.util;

import com.util.batch.domain.BatchScheduleDto;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.text.ParseException;

import static org.springframework.scheduling.support.CronExpression.isValidExpression;

/***************************************************
 *
 * 업무 그룹명 :
 * 서브 업무명 :
 * 설      명 :
 * 작   성  자 : Administrator
 * 작   성  일 : 2024-01-02
 * Copyright ⓒ SK C&C. All Right Reserved
 * ======================================
 * 변경자/변경일 :
 * 변경사유/내역 :
 * ======================================
 *
 ****************************************************/
public class JobUtil {
    /** job 생성 및 Factory 등록 */
    public static JobDetail createJob(BatchScheduleDto jobRequest, Class<? extends Job> jobClass, ApplicationContext context) {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(false);
        factoryBean.setApplicationContext(context);
        factoryBean.setName(jobRequest.getName());
        factoryBean.setGroup(jobRequest.getName());
        if (jobRequest.getJobDataMap() != null) {
            factoryBean.setJobDataMap(jobRequest.getJobDataMap());
        }
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
    /** job 상태 수정 */
    public static JobDetail updateJob(BatchScheduleDto batchJobDto, JobDetail oldJobDetail) {
        JobDataMap jobDataMap = oldJobDetail.getJobDataMap();
        jobDataMap.put("name", batchJobDto.getName());
        jobDataMap.put("description", batchJobDto.getDescription());
        jobDataMap.put("url", batchJobDto.getUrl());

        JobBuilder jobBuilder = oldJobDetail.getJobBuilder();
        return jobBuilder.usingJobData(jobDataMap).storeDurably().build();
    }
    /** Trigger 생성 */
    public static Trigger createTrigger(BatchScheduleDto jobRequest) throws ParseException {
        String cronExpression = jobRequest.getCronExpression();
        if (!cronExpression.isEmpty()) {
            if (!isValidExpression(cronExpression)) {
                throw new ParseException("Provided expression " + cronExpression + " is not a valid cron expression", 0);
            }
            CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
            factoryBean.setName(jobRequest.getName());
            factoryBean.setGroup(jobRequest.getName());
            factoryBean.setCronExpression(jobRequest.getCronExpression());
            factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
            factoryBean.afterPropertiesSet();
            return factoryBean.getObject();
        }
        throw new IllegalStateException("unsupported trigger descriptor");
    }
}
