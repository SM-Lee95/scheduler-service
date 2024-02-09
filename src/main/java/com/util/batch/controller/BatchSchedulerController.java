package com.util.batch.controller;

import com.util.batch.config.CronBatchJob;
import com.util.batch.domain.BaseResponseBody;
import com.util.batch.domain.BatchHistoryDto;
import com.util.batch.domain.BatchScheduleDto;
import com.util.batch.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class BatchSchedulerController {

    private final BatchService batchService;
    /** Batch Job 등록 */
    @PostMapping("/job")
    public ResponseEntity<BaseResponseBody> insertBatchJob(@RequestBody BatchScheduleDto batchScheduleDto) throws Exception {
        batchService.addJob(batchScheduleDto, CronBatchJob.class);
        return ResponseEntity.ok(BaseResponseBody.of(200, "Success"));
    }
    /** 등록된 Job List 조회 */
    @GetMapping("/list")
    public ResponseEntity<List<BatchScheduleDto>> selectBatchJobList() throws SchedulerException {
        return ResponseEntity.ok(batchService.getAllJobs());
    }
    /** 등록된 Job 삭제 */
    @DeleteMapping("/job")
    public ResponseEntity<BaseResponseBody> deleteBatchJob(@RequestParam(name="id") List<Long> batchJobList) throws Exception {
        batchService.deleteJob(batchJobList);
        return ResponseEntity.ok(BaseResponseBody.of(200, "Success"));
    }
    /** 등록된 Job 정지 */
    @PutMapping("/job/pause")
    public ResponseEntity<BaseResponseBody> pauseBatchJob(@RequestParam(name="id") List<Long> batchJobList) throws Exception {
        batchService.pauseJob(batchJobList);
        return ResponseEntity.ok(BaseResponseBody.of(200, "Success"));
    }
    /** 정지된 Job 재개 */
    @PutMapping("/job/resume")
    public ResponseEntity<BaseResponseBody> resumeBatchJob(@RequestParam(name="id") List<Long> batchJobList) throws Exception {
        batchService.resumeJob(batchJobList);
        return ResponseEntity.ok(BaseResponseBody.of(200, "Success"));
    }
    /** Job 즉시 실행 */
    @PutMapping("/job/execute")
    public ResponseEntity<BaseResponseBody> startBatchJob(@RequestBody BatchScheduleDto batchScheduleDto) throws Exception {
        batchService.callBatchJob(batchScheduleDto.getName());
        return ResponseEntity.ok(BaseResponseBody.of(200, "Success"));
    }
    /** Job 히스토리 조회 */
    @GetMapping("/history")
    public ResponseEntity<List<BatchHistoryDto>>  selectBatchJobHistoryList(@RequestParam(name="id") Long id) {
        return ResponseEntity.ok(batchService.getAllJobHistoryList(id));
    }
}
