package com.whc.crawler.collector.impl;

import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service("boluobao")
@Scope(value = "prototype")
public class BoLuoBaoCollector extends BaseCollector {
    private static final String URL_LIST = "https://www.heiyan.com/top/gengxin/day";
    private static final String CATALOG_URL = "https://www.heiyan.com/list/{0}.html";

    @Override
    public List<Catalog> grabCateLog(Novel result) {
        return super.grabCateLog(result);
    }

    @Override
    public List<Novel> grabNovel() {
        return super.grabNovel();
    }

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect(URL_LIST).get();
        System.out.println(document);
    }
}