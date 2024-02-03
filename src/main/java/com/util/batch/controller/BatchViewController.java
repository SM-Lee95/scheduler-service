package com.util.batch.controller;

import com.util.batch.domain.BatchScheduleDto;
import com.util.batch.util.ApiResponse;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * packageName    : com.util.batch.controller
 * fileName       : BatchViewController
 * author         : iseongmu
 * date           : 1/30/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 1/30/24        iseongmu       최초 생성
 */
@Controller
@RequestMapping("/view")
public class BatchViewController {
    @GetMapping("/list")
    public ApiResponse<List<BatchScheduleDto>> selectBatchJobList() throws SchedulerException {
        return new ApiResponse<>(true, null);
    }
}
