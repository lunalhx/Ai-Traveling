package com.example.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.travel.dto.CheckinRequestDTO;
import com.example.travel.entity.CheckinRecord;
import com.example.travel.vo.CheckinResultVO;
import com.example.travel.vo.CheckinRecordVO;
import com.example.travel.vo.PageResult;

public interface CheckinService extends IService<CheckinRecord> {

    CheckinResultVO checkin(CheckinRequestDTO requestDTO);

    Object queryTodayStatus(Long spotId);

    PageResult<CheckinRecordVO> queryMyRecords(Integer page, Integer pageSize);
}
