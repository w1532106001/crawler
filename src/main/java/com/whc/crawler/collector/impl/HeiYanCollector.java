package com.whc.crawler.collector.impl;

import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service("HeiYan")
@Scope(value = "prototype")
@Slf4j
public class HeiYanCollector extends BaseCollector {
    private static final String URL_LIST = "https://www.heiyan.com";
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
