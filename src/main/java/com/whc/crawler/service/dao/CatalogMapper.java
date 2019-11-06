package com.whc.crawler.service.dao;

import com.whc.crawler.entity.Catalog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CatalogMapper {

    int insertForeach(List<Catalog> list);

}
