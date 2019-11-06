package com.whc.crawler.repository;

import com.whc.crawler.entity.Catalog;
import com.whc.crawler.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelRepository extends JpaRepository<Novel,Integer> {
    Novel findNovelByUrl(String url);
}
