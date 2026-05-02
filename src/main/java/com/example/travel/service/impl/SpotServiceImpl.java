package com.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.entity.Spot;
import com.example.travel.mapper.SpotMapper;
import com.example.travel.service.SpotService;
import org.springframework.stereotype.Service;

@Service
public class SpotServiceImpl extends ServiceImpl<SpotMapper, Spot> implements SpotService {
}
