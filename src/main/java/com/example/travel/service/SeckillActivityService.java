package com.example.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.travel.entity.SeckillActivity;
import com.example.travel.vo.PageResult;
import com.example.travel.vo.SeckillActivityVO;

public interface SeckillActivityService extends IService<SeckillActivity> {

    PageResult<SeckillActivityVO> listActivities(Integer page, Integer pageSize);

    SeckillActivityVO getActivityDetail(Long activityId);

    Integer loadActivityStock(Long activityId);
}
