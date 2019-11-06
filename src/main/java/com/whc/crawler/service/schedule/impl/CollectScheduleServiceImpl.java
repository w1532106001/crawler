package com.whc.crawler.service.schedule.impl;

import com.whc.crawler.collector.CollectTask;
import com.whc.crawler.service.schedule.ICollectScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gaocb
 * @date 2018/11/20
 */
@Service("collectScheduleService")
public class CollectScheduleServiceImpl implements ICollectScheduleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectScheduleServiceImpl.class.getName());

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ScheduleService #" + mCount.getAndIncrement());
        }
    };

    private static final ScheduledThreadPoolExecutor SCHEDULED_EXECUTOR =
            new ScheduledThreadPoolExecutor(CORE_POOL_SIZE, THREAD_FACTORY);

    private static final String[] ARRAY_COLLECTOR = {
            "SeventeenK",
            "QiDian"
    };

    private static final List<String> COLLECTOR_NAMES = Arrays.asList(ARRAY_COLLECTOR);
    /**
     * 定时任务列表
     */
    private static final Map<String, CollectTask> TASKS = new ConcurrentHashMap<>();


    @Resource
    private ApplicationContext applicationContext;

    @Override
    public void executeSchedule() {
        for (String collector : COLLECTOR_NAMES) {
            CollectTask collectTask = null;

            LOGGER.info("collector name is " + collector);
            if (TASKS.containsKey(collector)) {
                collectTask = TASKS.get(collector);
            }
            if (collectTask == null) {
                collectTask = (CollectTask) applicationContext.getBean("collectTask");
                collectTask.initCollector(collector);

            }
            SCHEDULED_EXECUTOR.execute(collectTask);
            TASKS.put(collector, collectTask);
        }
    }
}
