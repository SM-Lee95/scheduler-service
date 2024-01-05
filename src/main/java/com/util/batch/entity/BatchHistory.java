package com.util.batch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

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
@Entity(name = "batch_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchHistory extends BaseEntity {
    @Id
    private Long id;
    private LocalDateTime startDati;
    private LocalDateTime endDati;
    private Long excTime;
    private String resMsg;
    private String resStatus;
    private String isManual;
    @ManyToOne
    @JoinColumn(name = "batch_id")
    private BatchSchedule batchSchedule;
    public void calculateExecutionTime() {
        this.setExcTime(Duration.between(startDati, endDati).getSeconds());
    }
}
