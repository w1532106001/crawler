package com.whc.crawler.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class Catalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * 章节名
     */
    private String name;
    /**
     * 书籍Id
     */
    private Integer novelId;
    /**
     * 来源章节Id
     */
    private String originCatalogId;
    /**
     * 章节序号
     */
    private Integer sortNum;
    /**
     * 免费章节 true是
     */
    private Boolean isFree;

    /**
     * 字数
     */
    private Integer wordCount;
    /**
     * 更新时间
     */
    private Date createDate;
    /**
     * 抓取时间
     */
    private Date grabDate;
}
