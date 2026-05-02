package com.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.entity.SpotCategory;
import com.example.travel.mapper.SpotCategoryMapper;
import com.example.travel.service.SpotCategoryService;
import org.springframework.stereotype.Service;

@Service
public class SpotCategoryServiceImpl extends ServiceImpl<SpotCategoryMapper, SpotCategory> implements SpotCategoryService {
}
