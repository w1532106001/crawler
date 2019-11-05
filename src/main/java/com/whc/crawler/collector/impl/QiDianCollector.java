package com.whc.crawler.collector.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.whc.crawler.collector.ICollector;
import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("QiDian")
@Scope(value = "prototype")
public class QiDianCollector implements ICollector {
    private static final String URL_LIST = "https://www.qidian.com/all?style=1&pageSize=20&siteid=1&pubflag=0&hiddenField=0";
    private static final String CATALOG_URL = "https://book.qidian.com/ajax/book/category?bookId=";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
    @Override
    public List<Catalog> grabCateLog(Novel novel) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(CATALOG_URL+novel.getOriginBookId()).method(Connection.Method.GET).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) {
            return null;
        }
        JSONObject body = JSONObject.parseObject(response.body());
        if(body==null){
            return null;
        }
        JSONObject data = body.getJSONObject("data");
        if(data==null){
            return null;
        }
        JSONArray vs = data.getJSONArray("vs");
        if(vs==null||vs.size()==0){
            return null;
        }
        List<Catalog> catalogList = new ArrayList<>();
        Catalog catalog;
        for (Object v : vs) {
            JSONArray cs = ((JSONObject)v).getJSONArray("cs");
            if(cs==null||cs.size()==0){
                return null;
            }
            for (Object c : cs) {
                catalog = new Catalog();
                JSONObject item = (JSONObject)c;
                if(item==null){
                    return null;
                }
                catalog.setName(item.getString("cN").trim());
                catalog.setOriginCatalogId(item.getString("id"));
                catalog.setWordCount(item.getInteger("cnt"));
                catalog.setIsFree(item.getInteger("sS")==1);
                catalog.setSortNum(item.getInteger("uuid"));
                try {
                    catalog.setCreateDate(sdf.parse(item.getString("uT")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                catalog.setGrabDate(new Date());
                catalog.setNovelId(novel.getId());
                catalogList.add(catalog);
            }
        }
        return catalogList;
    }


    @Override
    public List<Novel> grabNovel(Integer num) {
        Document document = null;
        try {
            document = Jsoup.connect(URL_LIST+"&page="+num).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (document == null) {
            return null;
        }
        Elements bookList = document.getElementsByClass("all-book-list");
        if (bookList == null || bookList.size() == 0) {
            return null;
        }
        Elements lis = bookList.select("li");
        if (lis == null || lis.size() == 0) {
            return null;
        }
        List<Novel> novelList = new ArrayList<>();
        Novel novel;
        for (Element li : lis) {
            novel = new Novel();
            String name = li.select("h4").text();
            if (StringUtils.isNotBlank(name)) {
                novel.setName(name.trim());
            }
            String url = li.select("h4").select("a").attr("href");
            if (StringUtils.isNotBlank(url)) {
                novel.setUrl(url.trim());
            }
            Elements authorElements = li.getElementsByClass("author");
            if (authorElements == null || authorElements.size() == 0) {
                return null;
            }
            String coverUrl = authorElements.select("img").attr("src");
            if (StringUtils.isNotBlank(coverUrl)) {
                novel.setCoverUrl(coverUrl);
            }
            Elements names = li.getElementsByClass("name");
            if(names==null||names.size()==0){
                return null;
            }
            for (Element element : names) {
                if(element.hasAttr("data-eid")){
                    String author = element.text();
                    if (StringUtils.isNotBlank(author)) {
                        novel.setAuthor(author);
                    }
                }
            }

            String category = authorElements.select("a").get(1).text();
            if (StringUtils.isNotBlank(category)) {
                novel.setCategory(category);
            }
            String statusString = authorElements.select("span").text();
            if (StringUtils.isNotBlank(statusString)) {
                switch (statusString) {
                    case "连载":
                        novel.setStatus(Byte.valueOf("0"));
                        break;
                    case "完结":
                        novel.setStatus(Byte.valueOf("1"));
                        break;
                    default:
                        novel.setStatus(null);
                        break;
                }
                String summary = li.getElementsByClass("intro").text();
                if (StringUtils.isNotBlank(summary)) {
                    novel.setSummary(summary);
                }
                novel.setCreateDate(null);
                novel.setGrabDate(new Date());
                novel.setOrigin("qidian");
                String originBookId = li.select("a").attr("data-bid");
                if(StringUtils.isNotBlank(originBookId)){
                    novel.setOriginBookId(originBookId);
                }
            }
            novelList.add(novel);
        }
        return novelList;
    }

    @Override
    public Integer grabNovelPageNum() {
        return 999;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Jsoup.connect("https://m.qidian.com/rank/yuepiao/male").get());
    }
}
