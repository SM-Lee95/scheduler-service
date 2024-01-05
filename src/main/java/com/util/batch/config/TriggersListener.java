package com.util.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.springframework.stereotype.Component;

/** Trigger가 발생할 때, 불발이 일어날 때, 완료될 때 이벤트를 받는 인터페이스 */
@Slf4j
@Component
public class TriggersListener implements TriggerListener {
    @Override
    public String getName() {
        return "globalTrigger";
    }
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        JobKey jobKey = trigger.getJobKey();
        log.info("triggerFired at {} :: jobKey : {}", trigger.getStartTime(), jobKey);
    }
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }
    @Override
    public void triggerMisfired(Trigger trigger) {
        JobKey jobKey = trigger.getJobKey();
        log.info("triggerMisfired at {} :: jobKey : {}", trigger.getStartTime(), jobKey);
    }
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
                                Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        JobKey jobKey = trigger.getJobKey();
        log.info("triggerComplete at {} :: jobKey : {}", trigger.getStartTime(), jobKey);
    }
}
