package com.util.batch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchHistoryDto {
    private Long id;
    private String groupName;
    private String jobName;
    private LocalDateTime startDati;
    private LocalDateTime endDati;
    private Long excTime;
    private String resMsg;
    private String resStatus;
    private String isManual;
}
