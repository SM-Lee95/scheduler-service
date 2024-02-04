package com.util.batch.service;

import com.util.batch.domain.BaseResponseBody;
import com.util.batch.domain.BatchScheduleDto;
import com.util.batch.entity.BatchHistory;
import com.util.batch.entity.BatchSchedule;
import com.util.batch.repository.BatchHistoryRepository;
import com.util.batch.repository.BatchRepository;
import com.util.batch.util.JobUtil;
import com.util.batch.util.NateOnRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
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

    /** 공통 API 호출용 서비스 */
    public void callBatchURL(JobKey jobKey, String jobUrl, String isManual) {
        log.info("Call Controller << URL : {}, isManual : {} >>", BATCH_URL + jobUrl, isManual);
        long startTime = System.currentTimeMillis();
        BaseResponseBody apiResponse= BaseResponseBody.of(400, null);
        try {
            apiResponse = WebClient.create(BATCH_URL).get()
                    .uri(jobUrl)
                    .retrieve()
                    .bodyToMono(BaseResponseBody.class)
                    .block();
        } catch (Exception e) {
            log.error(e.getMessage());
            apiResponse.setMessage(e.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            saveExecutionLog(jobKey, apiResponse, startTime, endTime, isManual);
            if (apiResponse.getStatusCode() != 200) {
                nateOnRequest.setContent(jobKey.getName(), apiResponse.getMessage());
                nateOnRequest.callAPI();
            }
        }
    }
    /** 공통 API 호출 시 로깅 */
    public void saveExecutionLog(JobKey jobKey, BaseResponseBody apiResponse, long startTime, long endTime, String isManual) {
        String errorMessage = "";
        if (apiResponse.getStatusCode() != 200) errorMessage = apiResponse.getMessage();
        BatchHistory batchHistory = BatchHistory.builder()
                .batchSchedule(batchRepository.findByName(jobKey.getName()).get())
                .startDate(LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC))
                .endDate(LocalDateTime.ofEpochSecond(endTime, 0, ZoneOffset.UTC))
                .isManual(isManual)
                .resultMsg(errorMessage)
                .resultStatus(apiResponse.getStatusCode() == 200 ? "S" : "F")
                .build();
        batchHistory.calculateExecutionTime();
        log.info(batchHistory.toString());
        batchHistoryRepository.save(batchHistory);
    }

    /** 전체 등록된 Job 조회 */
    @SuppressWarnings("unchecked")
    public List<BatchScheduleDto> getAllJobs() throws SchedulerException {
        List<BatchScheduleDto> jobs = new ArrayList<>();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                JobDataMap dataMap = getJobDataMap(jobKey);
                BatchScheduleDto jobDto = BatchScheduleDto.builder()
                        .name(jobKey.getName())
                        .url((String) dataMap.get("url"))
                        .description((String) dataMap.get("description"))
                        .cronExpression(getCronExpression(jobKey))
                        .build();
                jobs.add(jobDto);
            }
        }
        return jobs;
    }
    /** 스캐줄러 추가 및 잡 등록 */
    public boolean addJob(BatchScheduleDto jobDto, Class<? extends Job> jobClass) throws Exception {
        try {
            JobKey jobKey = JobKey.jobKey(jobDto.getName(), jobDto.getName());
            if (isJobExists(jobKey)) {
                log.info("[BatchSchedulerService] Job is already exists.");
                return false;
            }
            jobDto.setJobDataMapSelf();
            Trigger trigger = JobUtil.createTrigger(jobDto);
            JobDetail jobDetail = JobUtil.createJob(jobDto, jobClass, context);
            schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
            batchRepository.save(modelMapper.map(jobDto, BatchSchedule.class));
            return true;
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while scheduling", e);
        } catch (ParseException e) {
            throw new Exception("Cron Expression이 유효하지 않습니다.");
        }
        return false;
    }
    /** 스캐줄러 삭제 및 잡 삭제 */

    public void deleteJob(List<BatchScheduleDto> jobDtos) throws Exception {
        JobKey jobKey = null;
        try {
            for (BatchScheduleDto jobDto : jobDtos) {
                jobKey = new JobKey(jobDto.getName(), jobDto.getName());
                if (!isJobExists(jobKey)) {
                    throw new Exception("Job does not exits");
                }
                log.debug("[BatchSchedulerService] deleting job with jobKey : {}", jobKey);
                boolean deleteResult = schedulerFactoryBean.getScheduler().deleteJob(jobKey);
                if (!deleteResult) {
                    throw new Exception("Job does not deleted");
                }
                List<Long> idList = jobDtos.stream().map(BatchScheduleDto::getId).collect(Collectors.toList());
                batchRepository.deleteAllById(idList);
            }
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while deleting job with jobKey : {}", jobKey, e);
        }
    }
    /** 스캐줄러 정지 및 실행중인 잡 정지 */
    public void pauseJob(List<BatchScheduleDto> jobDtos) throws Exception {
        JobKey jobKey = null;
        try {
            for (BatchScheduleDto jobDto : jobDtos) {
                jobKey = new JobKey(jobDto.getName(), jobDto.getName());
                if (!isJobExists(jobKey)) {
                    throw new Exception("Job does not exits");
                }
                log.debug("[BatchSchedulerService] pausing job with jobKey : {}", jobKey);
                schedulerFactoryBean.getScheduler().pauseJob(jobKey);
            }
            batchRepository.findAllById(jobDtos.stream().map(BatchScheduleDto::getId).collect(Collectors.toList())).forEach(vo-> vo.setPauseYn("Y"));
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while pausing job with jobKey : {}", jobKey, e);
        }
    }
    /** 정지중인 잡 및 스캐줄러 재개 */
    public void resumeJob(List<BatchScheduleDto> jobDtos) throws Exception {
        JobKey jobKey = null;
        try {
            for (BatchScheduleDto jobDto : jobDtos) {
                jobKey = new JobKey(jobDto.getName(), jobDto.getName());
                if (!isJobExists(jobKey)) {
                    throw new Exception("Job does not exits");
                }
                if ("".equals(getJobState(jobKey))) {
                    throw new Exception("Job is not in paused state");
                }
                log.debug("[BatchSchedulerService] resuming job with jobKey : {}", jobKey);
                schedulerFactoryBean.getScheduler().resumeJob(jobKey);
            }
            batchRepository.findAllById(jobDtos.stream().map(BatchScheduleDto::getId).collect(Collectors.toList())).forEach(vo-> vo.setPauseYn("N"));
        } catch (SchedulerException e) {
            log.error("[BatchSchedulerService] error occurred while resuming job with jobKey : {}", jobKey, e);
        }
    }
    /** 등록된 잡 및 스캐줄러 업데이트 */
    public boolean updateJob(BatchScheduleDto jobRequestDto, Class<? extends Job> jobClass, String user) {
        try {
            JobKey jobKey = JobKey.jobKey(jobRequestDto.getName(), jobRequestDto.getName());
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
    /** Job 즉시 실행 */
    public boolean callBatchJob(String name) throws Exception {
        JobKey jobKey = JobKey.jobKey(name, name);
        JobDataMap jobDataMap = schedulerFactoryBean.getScheduler().getJobDetail(jobKey).getJobDataMap();
        String jobUrl = jobDataMap.getString("url");
        callBatchURL(jobKey, jobUrl, "Y");
        return true;
    }
    /** 현재 Job 실행 여부 */
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
    /** JobKey에 해당하는 상세 내역 반환 */
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
    /** JobKey에 해당하는 Trigger Cron Expression 반환  */
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
    /** JobKey에 해당하는 Trigger Cron Expression 반환  */
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
    /** JobKey에 해당하는 Job 상태 반환  */
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
    /** JobKey에 해당하는 Job 존재 여부  */
    private boolean isJobExists(JobKey jobKey) {
        try {
            return schedulerFactoryBean.getScheduler().checkExists(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }
}
