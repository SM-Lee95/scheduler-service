package com.util.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Properties;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class QuartzConfig {

    private final JobsListener jobsListener;
    private final TriggersListener triggersListener;
    private final QuartzProperties quartzProperties;
    /**
     * Quartz 관련 설정
     * SchedulerFactoryBean 은 ApplicationContext 에서 LifeCycle 형식으로 Scheduler 를 관리함
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) {
        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setApplicationContext(applicationContext);
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactoryBean.setGlobalTriggerListeners(triggersListener);
        schedulerFactoryBean.setGlobalJobListeners(jobsListener);
        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        return schedulerFactoryBean;
    }
}
