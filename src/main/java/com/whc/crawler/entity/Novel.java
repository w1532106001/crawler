package com.whc.crawler.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * 唯一标识
     */
    private String url;
    /**
     * 小说名
     */
    private String name;
    /**
     * 作者
     */
    private String author;
    /**
     * 封面
     */
    private String coverUrl;
    /**
     * 简介
     */
    private String summary;
    /**
     * 分类
     */
    private String category;
    /**
     * 0连载或1完结
     */
    private Byte status;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 抓取时间
     */
    private Date grabDate;
    /**
     * 来源
     */
    private String origin;
    /**
     * 来源BookId
     */
    private String originBookId;
}
