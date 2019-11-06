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

@Service("SeventeenK")
@Scope(value = "prototype")
@Slf4j
public class SeventeenKCollector extends BaseCollector {
    private static final String URL_LIST = "https://www.17K.com";
    private static final String CATALOG_URL = "https://www.17K.com/list/{0}.html";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
//    private static final Calendar calendar = Calendar.getInstance();
//
//    @Resource
//    private CatalogRepository catalogRepository;

    @Override
    public List<Catalog> grabCateLog(Novel novel) {
        Document document = null;
        try {
            document = Jsoup.connect(CATALOG_URL.replace("{0}",novel.getOriginBookId())).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (document == null) {
            return null;
        }
        Elements volume = document.getElementsByClass("Volume");
        if (volume == null||volume.size()==0) {
            return null;
        }
        volume.select("a.folding").remove();

        Elements lines = volume.select("a");
        if (lines == null||lines.size()==0) {
            return null;
        }
        List<Catalog> catalogList = new ArrayList<>();
        Catalog catalog;
        for (Element line : lines) {
            catalog = new Catalog();
            if (line == null) {
                continue;
            }
            catalog.setName(line.text().trim());
            String title = line.attr("title");
            if(StringUtils.isNotBlank(title)){
                String[] titles = title.split("\r");
                if(titles.length > 0){
                    for (String s : titles) {
                        if(s.contains("字数：")){
                            catalog.setWordCount(Integer.valueOf(s.replace("字数：","").trim()));
                        }
                        if(s.contains("更新日期:")){
                            try {
                                String time = s.replace("更新日期:","");
                                if(StringUtils.isNotBlank(time)){
                                    catalog.setCreateDate(sdf.parse(time.trim()));
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            String url = line.attr("href");
            if(StringUtils.isNotBlank(url)){
                String catalogId = url.replace("/","")
                        .replace("chapter","")
                        .replace(".html","")
                        .replace(novel.getOriginBookId(),"");
                if(StringUtils.isNotBlank(catalogId)){
                    catalog.setOriginCatalogId(catalogId);
                }
            }
            catalog.setIsFree(line.select("span").hasClass("vip"));

            catalog.setGrabDate(new Date());
            catalog.setNovelId(novel.getId());
            catalogList.add(catalog);

        }

        return catalogList;
    }

//    @Override
//    public List<Catalog> grabUpdate(Novel novel) {
//        List<Catalog> catalogList = grabCateLog(novel);
//        List<Catalog> resultCatalogList = null;
//        try {
//            resultCatalogList = catalogList.stream().filter(e->{
//                        Date date = new Date();
//                        calendar.setTime(date);
//                        calendar.add(Calendar.DAY_OF_MONTH, -1);
//                        return sdf2.format(e.getCreateDate()).equals(sdf2.format(date))||sdf2.format(e.getCreateDate())
//                                .equals(sdf2.format(calendar.getTime()));
//                    }
//            ).filter(e-> null == catalogRepository.findByNameAndNovelIdAndOriginCatalogId(e.getName(),
//                    e.getNovelId(),e.getOriginCatalogId())).collect(Collectors.toList());
//        }catch (Exception e){
//            e.printStackTrace();
//            System.out.println(catalogList);
//            for (Catalog c:
//                    catalogList) {
//                Date date = new Date();
//                calendar.setTime(date);
//                calendar.add(Calendar.DAY_OF_MONTH, -1);
//                Boolean b = sdf2.format(c.getCreateDate()).equals(sdf2.format(date))||sdf2.format(c.getCreateDate())
//                        .equals(sdf2.format(calendar.getTime()));
//                System.out.println(b);
//            }
//        }
//        return resultCatalogList;
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
        Elements PHB_CONT = document.getElementsByClass("PHB_CONT");
        if (PHB_CONT == null || PHB_CONT.size() == 0) {
            return null;
        }
        Element updateList = PHB_CONT.get(0);
        if (updateList == null) {
            return null;
        }
        Elements lis = updateList.select("li");
        if (lis == null || lis.size() == 0) {
            return null;
        }
        List<Novel> novelList = new ArrayList<>();
        Novel novel;
        for (Element li : lis) {
            novel = new Novel();
            String name = li.select("a").text();
            if (StringUtils.isNotBlank(name)) {
                novel.setName(name.trim());
            }
            String url = li.select("a").attr("href");
            if (StringUtils.isNotBlank(url)) {
                novel.setUrl(url.trim());
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
            Element bookInfo = infoDocument.getElementById("bookInfo");
            if (bookInfo == null) {
                return null;
            }
            String category = bookInfo.select("tbody").select("a").get(0).text();
            if (StringUtils.isNotBlank(category)) {
                novel.setCategory(category);
            }

            String summary = infoDocument.getElementsByClass("intro").text();
            if (StringUtils.isNotBlank(summary)) {
                novel.setSummary(summary);
            }

            String statusString = infoDocument.getElementsByClass("label").text();
            if (StringUtils.isNotBlank(statusString)) {
                if(statusString.contains("连载")){
                    novel.setStatus(Byte.valueOf("0"));
                }else if(statusString.contains("完结")){
                    novel.setStatus(Byte.valueOf("1"));
                }else {
                    novel.setStatus(null);
                }
            }


            String coverUrl = infoDocument.getElementsByClass("book").attr("src").trim();
            if (StringUtils.isNotBlank(coverUrl)) {
                novel.setCoverUrl(coverUrl);
            }

            String author = infoDocument.getElementsByClass("author").select("a.name").text();
            if (StringUtils.isNotBlank(author)) {
                novel.setAuthor(author);
            }



            novel.setCreateDate(null);
            novel.setGrabDate(new Date());
            novel.setOrigin("17K");
            String originBookId = novel.getUrl().replace("//www.17k.com/book/","")
                    .replace(".html","").trim();
            if (StringUtils.isNotBlank(originBookId)) {
                novel.setOriginBookId(originBookId);
            }


            novelList.add(novel);
        }
        return novelList;
    }

    public static void main(String[] args) throws IOException {

        Document document = Jsoup.connect("https://www.17k.com/book/3014711.html").get();
        System.out.println(document);
    }


}
