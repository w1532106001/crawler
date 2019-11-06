package com.whc.crawler.collector.impl;

import com.whc.crawler.collector.ICollector;
import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;
import com.whc.crawler.repository.CatalogRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseCollector implements ICollector {
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    private static final Calendar calendar = Calendar.getInstance();

    @Resource
    private CatalogRepository catalogRepository;

    @Override
    public List<Catalog> grabCateLog(Novel result) {
        return null;
    }

    @Override
    public List<Catalog> grabUpdate(Novel novel) {
        List<Catalog> catalogList = grabCateLog(novel);
        return catalogList.stream().filter(e -> {
                    Date date = new Date();
                    calendar.setTime(date);
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    return sdf2.format(e.getCreateDate()).equals(sdf2.format(date)) || sdf2.format(e.getCreateDate()).equals(sdf2.format(calendar.getTime()));
                }
        ).filter(e -> null == catalogRepository.findByNameAndNovelIdAndOriginCatalogId(e.getName(), e.getNovelId(), e.getOriginCatalogId())).collect(Collectors.toList());
    }

    @Override
    public List<Novel> grabNovel() {
        return null;
    }
}
