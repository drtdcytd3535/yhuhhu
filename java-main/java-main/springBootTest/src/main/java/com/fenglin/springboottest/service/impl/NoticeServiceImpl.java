package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.entity.Notice;
import com.fenglin.springboottest.mapper.NoticeMapper;
import com.fenglin.springboottest.service.INoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

    @Override
    public List<Notice> latest(int limit) {
        return this.lambdaQuery()
                .orderByAsc(Notice::getSortNo)
                .orderByDesc(Notice::getId)
                .last("limit " + Math.max(1, Math.min(limit, 20)))
                .list();
    }
}
