package com.whc.crawler.collector;


import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;

import java.io.IOException;
import java.util.List;

/**
 * @author gaocb
 * @date 2018/11/20
 */
public interface ICollector {

    /**
     *
     * @param result 书籍
     * @return 抓取目录信息
     */
    List<Catalog> grabCateLog(Novel result);

    /**
     *
     * @param num 页码
     * @return 抓取小说信息
     */
    List<Novel> grabNovel(Integer num);

    /**
     * @return 抓取小说分页size
     */
    Integer grabNovelPageNum();
}
