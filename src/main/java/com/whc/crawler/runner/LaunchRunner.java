package com.whc.crawler.runner;

import com.whc.crawler.service.schedule.ICollectScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author gaocb
 * @date 2018/11/20
 */
@Component
public class LaunchRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchRunner.class.getName());


    @Resource(name = "collectScheduleService")
    ICollectScheduleService collectScheduleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("采集任务启动");
        collectScheduleService.executeSchedule();
    }
}
