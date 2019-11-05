package com.whc.crawler.collector;

import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;
import com.whc.crawler.repository.CatalogRepository;
import com.whc.crawler.repository.NovelRepository;
import com.whc.crawler.service.dao.CatalogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private static final String INSERT_LIST_SQL = "insert into catalog(id,name,origin_catalog_id,sort_num," +
            "is_free,word_count,create_date,grab_date) values(null,?,?,?,?,?,?,?)";

    private ICollector collector;

    @Resource
    private ApplicationContext applicationContext;

    @Autowired
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
        int totalPage = collector.grabNovelPageNum();
        if (totalPage == 0) {
            return;
        }
        List<Novel> novelList;
        for (int i = 6; i <= totalPage; i++) {
            Long startTime = System.currentTimeMillis();

            novelList = collector.grabNovel(i);
            if (novelList == null) {
                LOGGER.error("第"+i+"页采集失败");
                continue;
            }
            for (Novel novel : novelList) {
                try {
                    Novel result = novelRepository.save(novel);
                    List<Catalog> catalogList = collector.grabCateLog(result);
                    if (catalogList != null && catalogList.size() > 0) {
                        //插入当前页novel 与catalogList
                        catalogMapper.insertForeach(catalogList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Long pageCollectTime = System.currentTimeMillis();
            LOGGER.info("第"+i+"页采集完成,耗时:"+(pageCollectTime-startTime)+"ms");
        }
    }
}
