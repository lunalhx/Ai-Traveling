package com.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.entity.CheckinRule;
import com.example.travel.mapper.CheckinRuleMapper;
import com.example.travel.service.CheckinRuleService;
import org.springframework.stereotype.Service;

@Service
public class CheckinRuleServiceImpl extends ServiceImpl<CheckinRuleMapper, CheckinRule> implements CheckinRuleService {
}
