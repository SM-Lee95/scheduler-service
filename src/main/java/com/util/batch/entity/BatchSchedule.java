package com.util.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

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
@Entity(name = "batch_schedule")
@Data
public class BatchSchedule extends BaseEntity {
    @Id
    private Long id;
    private String groupName;
    private String jobName;
    private String jobDesc;
    private String cronExp;
    private String trgName;
    private String url;
    private String pauseYn;
}
