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
     * 抓取目录信息
     * @param result 书籍
     * @return List<Catalog>
     */
    List<Catalog> grabCateLog(Novel result);

    /**
     * 抓取更新
     * @param result 书籍
     * @return List<Catalog>
     */
    List<Catalog> grabUpdate(Novel result);

    /**
     * 抓取最新更新小说信息
     * @return List<Novel>
     */
    List<Novel> grabNovel();

}
