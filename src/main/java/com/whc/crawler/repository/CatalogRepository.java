package com.whc.crawler.repository;

import com.whc.crawler.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Integer> {
    Catalog findByNameAndNovelIdAndOriginCatalogId(String name, Integer novelId, String originCatalogId);
}
