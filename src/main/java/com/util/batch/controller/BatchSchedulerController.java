package com.util.batch.controller;

import com.util.batch.config.CronBatchJob;
import com.util.batch.domain.BatchHistoryDto;
import com.util.batch.domain.BatchJobDto;
import com.util.batch.domain.BatchScheduleDto;
import com.util.batch.service.BatchService;
import com.util.batch.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class BatchSchedulerController {

    private final BatchService batchService;
    /** Batch Job 등록 */
    @PostMapping("/job")
    public ApiResponse<String> insertBatchJob(@RequestBody BatchScheduleDto jobDto) {
        return null;
    }
    @GetMapping("/list")
    public ApiResponse<List<BatchScheduleDto>> selectBatchJobList() throws SchedulerException {
        return new ApiResponse<>(true, batchService.getAllJobs());
    }

    @PutMapping("/job")
    public ApiResponse<String> updateBatchJob(@RequestBody BatchScheduleDto jobDto) {
        return null;
    }
    @DeleteMapping("/job")
    public ApiResponse<String> deleteBatchJob(@RequestBody List<BatchScheduleDto> jobDtoList) {
        return null;
    }
    @PutMapping("/job/pause")
    public ApiResponse<String> pauseBatchJob(@RequestBody List<BatchScheduleDto> jobDtoList) {
        return null;
    }
    @PutMapping("/job/resume")
    public ApiResponse<String> resumeBatchJob(@RequestBody List<BatchScheduleDto> jobDtoList) {
        return null;
    }
    @PutMapping("/job/execute")
    public ApiResponse<String> startBatchJob(@RequestBody BatchScheduleDto jobDto) {
        return null;
    }
    @GetMapping("/history")
    public ApiResponse<List<BatchHistoryDto>>  selectBatchJobHistoryList(BatchHistoryDto batchHistoryDto) {
        return null;
    }
}
