package com.util.batch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchHistory extends BaseEntity implements Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="start_date")
    private LocalDateTime startDate;
    @Column(name="end_date")
    private LocalDateTime endDate;
    @Column(name="exc_date")
    private Long excTime;
    @Column(name="result_msg")
    private String resultMsg;
    @Column(name="result_status")
    private String resultStatus;
    @Column(name="is_manual")
    private String isManual;
    @ManyToOne
    @JoinColumn(name = "batch_id")
    private BatchSchedule batchSchedule;
    public void calculateExecutionTime() {
        this.setExcTime(Duration.between(startDate, endDate).getSeconds());
    }
    @Override
    public boolean isNew() {
        return super.getRegDate() == null;
    }
}
