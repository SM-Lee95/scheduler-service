package com.util.batch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;

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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSchedule extends BaseEntity implements Persistable<Long> {
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
    @OneToMany(mappedBy = "batchSchedule", cascade = CascadeType.ALL)
    private List<BatchHistory> batchHistories = new ArrayList<>();
    @Override
    public boolean isNew() {
        return super.getRegDate() == null;
    }
}
