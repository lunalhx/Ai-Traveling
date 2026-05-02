package com.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.entity.CheckinRecord;
import com.example.travel.mapper.CheckinRecordMapper;
import com.example.travel.service.CheckinRecordService;
import org.springframework.stereotype.Service;

@Service
public class CheckinRecordServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord> implements CheckinRecordService {
}
