package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.service.SeckillActivityService;
import com.example.travel.vo.PageResult;
import com.example.travel.vo.SeckillActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeckillActivityController {

    private final SeckillActivityService seckillActivityService;

    @GetMapping("/seckill/activity/list")
    public Result<PageResult<SeckillActivityVO>> listActivities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(seckillActivityService.listActivities(page, pageSize));
    }

    @GetMapping("/seckill/activity/{activityId}")
    public Result<SeckillActivityVO> getActivityDetail(@PathVariable Long activityId) {
        return Result.success(seckillActivityService.getActivityDetail(activityId));
    }

    @PostMapping("/admin/seckill/activity/{activityId}/load")
    public Result<Integer> loadActivityStock(@PathVariable Long activityId) {
        return Result.success(seckillActivityService.loadActivityStock(activityId));
    }
}
