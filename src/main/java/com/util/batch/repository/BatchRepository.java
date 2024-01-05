package com.util.batch.repository;

import com.util.batch.entity.BatchSchedule;
import org.springframework.data.jpa.repository.JpaRepository;


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
public interface BatchRepository extends JpaRepository<BatchSchedule, Long> {
}
