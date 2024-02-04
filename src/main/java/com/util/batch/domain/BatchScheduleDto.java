package com.util.batch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.quartz.JobDataMap;

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
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchScheduleDto {
    private Long id;
    private String name;
    private String description;
    private String cronExpression;
    private String url;
    private String pauseYn;
    private String scheduleTime;
    private String lastFiredTime;
    private String nextFireTime;
    private JobDataMap jobDataMap;
    public void setJobDataMapSelf() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("name", this.name);
        jobDataMap.put("description", this.description);
        jobDataMap.put("url", this.url);

        this.jobDataMap = jobDataMap;
    }
}
