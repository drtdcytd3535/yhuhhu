package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.entity.Notice;

import java.util.List;

/**
 * 宗门公告服务
 */
public interface INoticeService extends IService<Notice> {

    /** 首页公告：按排序号取前 N 条 */
    List<Notice> latest(int limit);
}
