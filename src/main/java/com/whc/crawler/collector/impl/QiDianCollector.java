package com.whc.crawler.collector.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.whc.crawler.collector.ICollector;
import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;
import com.whc.crawler.repository.CatalogRepository;
import com.whc.crawler.utils.UserAgentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("QiDian")
@Scope(value = "prototype")
@Slf4j
public class QiDianCollector extends BaseCollector {
    private static final String URL_LIST = "https://www.qidian.com";
    private static final String CATALOG_URL = "https://book.qidian.com/ajax/book/category?bookId=";
//    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
//    private static final Calendar calendar = Calendar.getInstance();
//
//    @Resource
//    private CatalogRepository catalogRepository;

    @Override
    public List<Catalog> grabCateLog(Novel novel) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(CATALOG_URL + novel.getOriginBookId()).userAgent(UserAgentUtil.getRandomUserAgent()).method(Connection.Method.GET).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) {
            return null;
        }
        JSONObject body = JSONObject.parseObject(response.body());
        if (body == null) {
            return null;
        }
        JSONObject data = body.getJSONObject("data");
        if (data == null) {
            return null;
        }
        JSONArray vs = data.getJSONArray("vs");
        if (vs == null || vs.size() == 0) {
            return null;
        }
        List<Catalog> catalogList = new ArrayList<>();
        Catalog catalog;
        for (Object v : vs) {
            JSONArray cs = ((JSONObject) v).getJSONArray("cs");
            if (cs == null || cs.size() == 0) {
                return null;
            }
            for (Object c : cs) {
                catalog = new Catalog();
                JSONObject item = (JSONObject) c;
                if (item == null) {
                    return null;
                }
                catalog.setName(item.getString("cN").trim());
                catalog.setOriginCatalogId(item.getString("id"));
                catalog.setWordCount(item.getInteger("cnt"));
                catalog.setIsFree(item.getInteger("sS") == 1);
                catalog.setSortNum(item.getInteger("uuid"));
                catalog.setCreateDate(item.getDate("uT"));
                catalog.setGrabDate(new Date());
                catalog.setNovelId(novel.getId());
                catalogList.add(catalog);
            }
        }
        return catalogList;
    }

//    @Override
//    public List<Catalog> grabUpdate(Novel novel) {
//        List<Catalog> catalogList = grabCateLog(novel);
//        return catalogList.stream().filter(e->{
//                    Date date = new Date();
//                    calendar.setTime(date);
//                    calendar.add(Calendar.DAY_OF_MONTH, -1);
//                    return sdf2.format(e.getCreateDate()).equals(sdf2.format(date))||sdf2.format(e.getCreateDate()).equals(sdf2.format(calendar.getTime()));
//                }
//        ).filter(e-> null == catalogRepository.findByNameAndNovelIdAndOriginCatalogId(e.getName(), e.getNovelId(),e.getOriginCatalogId())).collect(Collectors.toList());
//    }


    @Override
    public List<Novel> grabNovel() {
        Document document = null;
        try {
            document = Jsoup.connect(URL_LIST).userAgent(UserAgentUtil.getRandomUserAgent()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (document == null) {
            return null;
        }
        Elements bookList = document.getElementsByClass("update-table all");
        if (bookList == null || bookList.size() == 0) {
            return null;
        }
        Elements trs = bookList.select("tr");
        if (trs == null || trs.size() == 0) {
            return null;
        }
        List<Novel> novelList = new ArrayList<>();
        Novel novel;
        for (Element tr : trs) {
            novel = new Novel();
            String name = tr.getElementsByClass("name").text();
            if (StringUtils.isNotBlank(name)) {
                novel.setName(name.trim());
            }
            String url = tr.getElementsByClass("name").attr("href");
            if (StringUtils.isNotBlank(url)) {
                novel.setUrl(url.trim());
            }
            Elements authorElements = tr.getElementsByClass("author");
            if (authorElements == null || authorElements.size() == 0) {
                return null;
            }

            Elements names = tr.getElementsByClass("name");
            if (names == null || names.size() == 0) {
                return null;
            }

            String author = authorElements.text();
            if (StringUtils.isNotBlank(author)) {
                novel.setAuthor(author);
            }

            String category = tr.getElementsByClass("classify").text().replace("「","").replace("」","");
            if (StringUtils.isNotBlank(category)) {
                novel.setCategory(category);
            }

            novel.setCreateDate(null);
            novel.setGrabDate(new Date());
            novel.setOrigin("qidian");
            String originBookId = tr.getElementsByClass("section").attr("data-bid");
            if (StringUtils.isNotBlank(originBookId)) {
                novel.setOriginBookId(originBookId);
            }
            Document infoDocument = null;
            try {
                infoDocument = Jsoup.connect(novel.getUrl().replace("//","https://")).userAgent(UserAgentUtil.getRandomUserAgent()).get();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("网络异常,url:"+novel.getUrl());
            }
            if(infoDocument==null){
                return null;
            }
            Elements bookInfo = infoDocument.getElementsByClass("book-info");
            if (bookInfo == null || bookInfo.size() == 0) {
                return null;
            }
            String statusString = bookInfo.select("span.blue").get(0).text();
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
            }
            String summary = bookInfo.select("p.intro").text();
            if (StringUtils.isNotBlank(summary)) {
                novel.setSummary(summary);
            }

            String coverUrl = infoDocument.select("img").attr("src").trim();
            if (StringUtils.isNotBlank(coverUrl)) {
                novel.setCoverUrl(coverUrl);
            }

            novelList.add(novel);
        }
        return novelList;
    }

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect("http://book.qidian.com/info/1013697581").get();
        System.out.println(document);
    }
}
