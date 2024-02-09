package com.util.batch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/***************************************************
 *
 * 업무 그룹명 :
 * 서브 업무명 :
 * 설      명 :
 * 작   성  자 : Administrator
 * 작   성  일 : 2024-01-04
 * Copyright ⓒ SK C&C. All Right Reserved
 * ======================================
 * 변경자/변경일 :
 * 변경사유/내역 :
 * ======================================
 *
 ****************************************************/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchHistoryDto {
    private Long id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long excTime;
    private String resultMsg;
    private String resultStatus;
    private String isManual;
}
