package com.util.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@Getter
@MappedSuperclass //JPA Entity 클래스들이 해당 추상 클래스를 상속할 경우 createDate, modifiedDate를 컬럼으로 인식
@EntityListeners(AuditingEntityListener.class) //해당 클래스에 Auditing 기능을 포함
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "reg_dati", updatable = false)
    private LocalDateTime regDate;
    @LastModifiedDate
    @Column(name = "modi_dati")
    private LocalDateTime modiDate;
    /** AuditorAware 구현 필요 */
    @CreatedBy
    @Column(updatable = false, name = "created_by")
    private String createdBy;
    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
    @Column(name = "sys_memo")
    private String sysMemo;
}
