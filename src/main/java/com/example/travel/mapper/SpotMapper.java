package com.example.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.travel.entity.Spot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpotMapper extends BaseMapper<Spot> {
}
