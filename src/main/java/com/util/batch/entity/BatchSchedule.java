package com.util.batch.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.Primary;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String name;
    private String description;
    @Column(name="cron_expression")
    private String cronExpression;
    private String url;
    @Column(name="pause_yn")
    private String pauseYn;
}
