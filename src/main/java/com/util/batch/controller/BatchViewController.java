package com.util.batch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * packageName    : com.util.batch.controller
 * fileName       : BatchViewController
 * author         : iseongmu
 * date           : 1/30/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 1/30/24        iseongmu       최초 생성
 */
@Controller
@RequestMapping("/view")
public class BatchViewController {
    @GetMapping("/info")
    public String schedulerHistoryInfo(){
        return "item";
    }
}
