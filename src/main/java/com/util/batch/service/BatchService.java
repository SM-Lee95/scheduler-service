package com.util.batch.service;

import com.util.batch.domain.BatchJobDto;
import com.util.batch.domain.BatchScheduleDto;
import com.util.batch.entity.BatchHistory;
import com.util.batch.entity.BatchSchedule;
import com.util.batch.repository.BatchHistoryRepository;
import com.util.batch.repository.BatchRepository;
import com.util.batch.util.ApiResponse;
import com.util.batch.util.JobUtil;
import com.util.batch.util.NateOnRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class BatchService {
    private final BatchHistoryRepository batchHistoryRepository;
    private final BatchRepository batchRepository;
    private final NateOnRequest nateOnRequest;
    private final SchedulerFactoryBean schedulerFactoryBean;
    private final ApplicationContext context;
    private final ModelMapper modelMapper;
    @Value("${batch.url}")
    private String BATCH_URL;

    public void callBatchURL(JobKey jobKey, String jobUrl, String isManual) {
        log.info("Call Controller << URL : {}, isManual : {} >>", BATCH_URL + jobUrl, isManual);
        long startTime = System.currentTimeMillis();
        ApiResponse<String> apiResponse = new ApiResponse();
        try {
            apiResponse = WebClient.create(BATCH_URL).get()
                    .uri(jobUrl)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();
        } catch (Exception e) {
            apiResponse.setData(e.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            saveExecutionLog(jobKey, apiResponse, startTime, endTime, isManual);
            if (!apiResponse.getSuccess()) {
                nateOnRequest.setContent(jobKey.getName(), apiResponse.getData());
                nateOnRequest.callAPI();
            }
        }
    }

    public void saveExecutionLog(JobKey jobKey, ApiResponse<String> apiResponse, long startTime, long endTime, String isManual) {
        String errorMessage = "";
        if (!apiResponse.getSuccess()) errorMessage = apiResponse.getData();
        BatchHistory batchHistory = BatchHistory.builder()
                .batchSchedule(batchRepository.findById(Long.valueOf(jobKey.getName())).get())
                .startDati(LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC))
                .endDati(LocalDateTime.ofEpochSecond(endTime, 0, ZoneOffset.UTC))
                .isManual(isManual)
                .resMsg(errorMessage)
                .resStatus(apiResponse.getSuccess() ? "S" : "F")
                .build();
        batchHistory.calculateExecutionTime();
        log.info(batchHistory.toString());
        batchHistoryRepository.save(batchHistory);
    }


    @SuppressWarnings("unchecked")
    public List<BatchScheduleDto> getAllJobs() throws SchedulerException {
        List<BatchScheduleDto> jobs = new ArrayList<>();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                JobDataMap dataMap = getJobDataMap(jobKey);
                BatchScheduleDto jobDto = BatchScheduleDto.builder()
                        .jobName(jobKey.getName())
                        .groupName(jobKey.getGroup())
                        .url((String) dataMap.get("url"))
                        .jobDesc((String) dataMap.get("description"))
                        .cronExp(getCronExpression(jobKey))
                        .build();
                jobs.add(jobDto);
            }
        }
        return jobs;
    }

    public boolean addJob(BatchJobDto jobDto, Class<? extends Job> jobClass, String user) {
        try {
            JobKey jobKey = JobKey.jobKey(jobDto.getJobName(), jobDto.getGroupName());
            if (isJobExists(jobKey)) {
                log.info("[BatchSchedulerService] Job is already exists.");
                return false;
            }
            jobDto.setJobDataMapSelf();
            Trigger trigger = JobUtil.createTrigger(jobDto);
            JobDetail jobDetail = JobUtil.createJob(jobDto, jobClass, context);
            Date date = schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
            batchRepository.save(modelMapper.map(jobDto, BatchSchedule.class));
            return true;
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while scheduling", e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void deleteJob(List<BatchJobDto> jobDtos) throws Exception {
        JobKey jobKey = null;
        try {
            for (BatchJobDto jobDto : jobDtos) {
                jobKey = new JobKey(jobDto.getJobName(), jobDto.getGroupName());
                if (!isJobExists(jobKey)) {
                    throw new Exception("Job does not exits");
                }
                log.debug("[BatchSchedulerService] deleting job with jobKey : {}", jobKey);
                boolean deleteResult = schedulerFactoryBean.getScheduler().deleteJob(jobKey);
                if (!deleteResult) {
                    throw new Exception("Job does not deleted");
                }
                List<Long> idList = jobDtos.stream().map(BatchJobDto::getId).collect(Collectors.toList());
                batchRepository.deleteAllById(idList);
            }
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while deleting job with jobKey : {}", jobKey, e);
        }
    }

    public void pauseJob(List<BatchJobDto> jobDtos) throws Exception {
        JobKey jobKey = null;
        try {
            for (BatchJobDto jobDto : jobDtos) {
                jobKey = new JobKey(jobDto.getJobName(), jobDto.getGroupName());
                if (!isJobExists(jobKey)) {
                    throw new Exception("Job does not exits");
                }
                log.debug("[BatchSchedulerService] pausing job with jobKey : {}", jobKey);
                schedulerFactoryBean.getScheduler().pauseJob(jobKey);
            }
            batchRepository.findAllById(jobDtos.stream().map(BatchJobDto::getId).collect(Collectors.toList())).forEach(vo-> vo.setPauseYn("Y"));
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while pausing job with jobKey : {}", jobKey, e);
        }
    }

    public void resumeJob(List<BatchJobDto> jobDtos) throws Exception {
        JobKey jobKey = null;
        try {
            for (BatchJobDto jobDto : jobDtos) {
                jobKey = new JobKey(jobDto.getJobName(), jobDto.getGroupName());
                if (!isJobExists(jobKey)) {
                    throw new Exception("Job does not exits");
                }
                if ("".equals(getJobState(jobKey))) {
                    throw new Exception("Job is not in paused state");
                }
                log.debug("[BatchSchedulerService] resuming job with jobKey : {}", jobKey);
                schedulerFactoryBean.getScheduler().resumeJob(jobKey);
            }
            batchRepository.findAllById(jobDtos.stream().map(BatchJobDto::getId).collect(Collectors.toList())).forEach(vo-> vo.setPauseYn("N"));
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while resuming job with jobKey : {}", jobKey, e);
        }
    }

    public boolean updateJob(BatchJobDto jobRequestDto, Class<? extends Job> jobClass, String user) {
        try {
            JobKey jobKey = JobKey.jobKey(jobRequestDto.getJobName(), jobRequestDto.getGroupName());
            if (!isJobExists(jobKey)) {
                log.info("[BatchSchedulerService] Job is not exists.");
                return false;
            }
            String jobStatus = getJobState(jobKey);
            jobRequestDto.setJobDataMapSelf();

            // update trigger
            TriggerKey oldTriggerKey = getTriggerKey(jobKey);
            Trigger trigger = JobUtil.createTrigger(jobRequestDto);
            Date date = schedulerFactoryBean.getScheduler().rescheduleJob(oldTriggerKey, trigger);

            // update job
            JobDetail newJobDetail = JobUtil.updateJob(jobRequestDto, schedulerFactoryBean.getScheduler().getJobDetail(jobKey));
            schedulerFactoryBean.getScheduler().addJob(newJobDetail, true);

            // check Job Status
            if ("PAUSED".equals(jobStatus)) {
                schedulerFactoryBean.getScheduler().pauseJob(jobKey);
            }
            log.debug("Job with jobKey : {} scheduled successfully at date : {}", jobKey, date);
            // update database
            /** 작성 필요 */
            return true;
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while scheduling", e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean callBatchJob(String groupName, String jobName, String user) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        JobDataMap jobDataMap = schedulerFactoryBean.getScheduler().getJobDetail(jobKey).getJobDataMap();
        String jobUrl = jobDataMap.getString("url");
        callBatchURL(jobKey, jobUrl, "Y");
        return true;
    }

    private boolean isJobRunning(JobKey jobKey) {
        try {
            List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
            if (currentJobs != null) {
                for (JobExecutionContext jobExecutionContext : currentJobs) {
                    if (jobKey.getName().equals(jobExecutionContext.getJobDetail().getKey().getName())) {
                        return true;
                    }
                }
            }
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while checking job with jobKey : {}", jobKey, e);
        }
        return false;
    }

    private JobDataMap getJobDataMap(JobKey jobKey) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            return jobDetail.getJobDataMap();
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] Error occurred while getting job state with jobKey : {}", jobKey, e);
        }
        return null;
    }

    private String getCronExpression(JobKey jobKey) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
            if (triggers != null && triggers.size() > 0) {
                for (Trigger trigger : triggers) {
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        return cronTrigger.getCronExpression();
                    }
                }
            }
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] Error occurred while getting job state with jobKey : {}", jobKey, e);
        }
        return null;
    }

    private TriggerKey getTriggerKey(JobKey jobKey) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());

            if (triggers != null && triggers.size() > 0) {
                for (Trigger trigger : triggers) {
                    return trigger.getKey();
                }
            }
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] Error occurred while getting Trigger Key with jobKey : {}", jobKey, e);
        }
        return null;
    }

    private String getJobState(JobKey jobKey) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());

            if (triggers != null && triggers.size() > 0) {
                for (Trigger trigger : triggers) {
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    if (Trigger.TriggerState.NORMAL.equals(triggerState)) {
                        return "SCHEDULED";
                    }
                    return triggerState.name().toUpperCase();
                }
            }
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] Error occurred while getting job state with jobKey : {}", jobKey, e);
        }
        return null;
    }

    private boolean isJobExists(JobKey jobKey) {
        try {
            return schedulerFactoryBean.getScheduler().checkExists(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }
}
