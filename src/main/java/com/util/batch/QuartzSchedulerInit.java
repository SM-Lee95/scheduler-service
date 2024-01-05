package com.util.batch;

import com.util.batch.config.CronBatchJob;
import com.util.batch.domain.BatchJobDto;
import com.util.batch.domain.BatchScheduleDto;
import com.util.batch.repository.BatchRepository;
import com.util.batch.util.JobUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuartzSchedulerInit {
    private final SchedulerFactoryBean schedulerFactoryBean;
    private final ApplicationContext context;
    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;

    @PostConstruct
    private void init() throws SchedulerException, ParseException {
        scheduleCronBatchJob();
    }
    /** 기동시 Schedule 등록 */
    private void scheduleCronBatchJob() throws SchedulerException, ParseException {
        log.info("Start scheduleCronBatchJob");
        List<BatchScheduleDto> BatchScheduleDtoList = batchRepository.findAll().stream()
                .map(vo->modelMapper.map(vo, BatchScheduleDto.class))
                .collect(Collectors.toList());
        for (BatchScheduleDto batchScheduleDto : BatchScheduleDtoList) {
            BatchJobDto BatchJobDto = makeBatchJobDto(batchScheduleDto);
            Trigger trigger = JobUtil.createTrigger(BatchJobDto);
            JobDetail jobDetail = JobUtil.createJob(BatchJobDto, CronBatchJob.class, context);
            schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
            if ("Y".equals(batchScheduleDto.getPauseYn())) {
                schedulerFactoryBean.getScheduler().pauseJob(JobKey.jobKey(BatchJobDto.getJobName(), BatchJobDto.getGroupName()));
            }
        }
    }
    private BatchJobDto makeBatchJobDto(BatchScheduleDto batchScheduleDto) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("name", batchScheduleDto.getJobName());
        jobDataMap.put("url", batchScheduleDto.getUrl());
        jobDataMap.put("description", batchScheduleDto.getJobDesc());
        return BatchJobDto.builder()
                .groupName(batchScheduleDto.getGroupName())
                .jobName(batchScheduleDto.getJobName())
                .cronExpression(batchScheduleDto.getCronExp())
                .jobDataMap(jobDataMap)
                .build();
    }
}
