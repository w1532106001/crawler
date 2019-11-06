package com.whc.crawler.collector;

import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;
import com.whc.crawler.repository.NovelRepository;
import com.whc.crawler.service.dao.CatalogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * 采集任务
 *
 * @author gaocb
 * @date 2018/11/20
 */

@Service("collectTask")
@Scope(value = "prototype")
public class CollectTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectTask.class.getName());

    private ICollector collector;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private NovelRepository novelRepository;
    @Resource
    private CatalogMapper catalogMapper;

    /**
     * 初始化采集者
     *
     * @param collectorName 采集者名称
     */
    public void initCollector(String collectorName) {
        this.collector = (ICollector) applicationContext.getBean(collectorName);
    }

    @Override
    public void run() {
        if (collector == null) {
            return;
        }
        System.out.println(collector);
        List<Novel> novelList;
        Long startTime = System.currentTimeMillis();
        novelList = collector.grabNovel();
        if (novelList == null) {
            return;
        }
        for (Novel novel : novelList) {
            try {
                //判断小说是否存在 存在增量更新不存在全量更新
                Novel resultNovel = novelRepository.findNovelByUrl(novel.getUrl());
                if(null==resultNovel){
                    synchronized (this){
                        Novel result = novelRepository.save(novel);
                        List<Catalog> catalogList = collector.grabCateLog(result);
                        if (catalogList != null && catalogList.size() > 0) {
                            //插入当前页novel 与catalogList
                            catalogMapper.insertForeach(catalogList);
                        }
                    }

                }else{
                    /**
                     * 更新章节
                     */
                    List<Catalog> catalogList = collector.grabUpdate(resultNovel);
                    if(null!=catalogList&&catalogList.size()>0){
                        synchronized (this){
                            catalogMapper.insertForeach(catalogList);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Long pageCollectTime = System.currentTimeMillis();
        LOGGER.info("采集完成,耗时:" + (pageCollectTime - startTime) + "ms");

    }
}
