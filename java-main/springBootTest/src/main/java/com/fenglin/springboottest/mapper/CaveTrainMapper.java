package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.CaveTrain;
import org.apache.ibatis.annotations.Mapper;

/**
 * 洞天修炼记录 Mapper
 */
@Mapper
public interface CaveTrainMapper extends BaseMapper<CaveTrain> {
}
