package com.whc.crawler.schedule;

import com.whc.crawler.service.schedule.ICollectScheduleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 定时任务
 *
 * @author gaocb
 */
@Component
public class ScheduleTask {

    @Resource(name = "collectScheduleService")
    ICollectScheduleService collectScheduleService;


    @Scheduled(cron = "0 0/1 * * * ?")
    private void startCollectSchedule() {
        collectScheduleService.executeSchedule();
    }

}
