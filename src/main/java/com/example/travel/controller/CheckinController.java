package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.CheckinRequestDTO;
import com.example.travel.service.CheckinService;
import com.example.travel.vo.CheckinRecordVO;
import com.example.travel.vo.CheckinResultVO;
import com.example.travel.vo.PageResult;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping("/checkin")
    public Result<CheckinResultVO> checkin(@RequestBody @Valid CheckinRequestDTO requestDTO) {
        return Result.success(checkinService.checkin(requestDTO));
    }

    @GetMapping("/checkin/today")
    public Result<Object> queryTodayStatus(@RequestParam(required = false) Long spotId) {
        return Result.success(checkinService.queryTodayStatus(spotId));
    }

    @GetMapping("/checkin/records")
    public Result<PageResult<CheckinRecordVO>> queryMyRecords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(checkinService.queryMyRecords(page, pageSize));
    }
}
