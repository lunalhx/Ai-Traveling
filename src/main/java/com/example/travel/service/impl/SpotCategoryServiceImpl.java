package com.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.entity.SpotCategory;
import com.example.travel.mapper.SpotCategoryMapper;
import com.example.travel.service.SpotCategoryService;
import com.example.travel.vo.SpotCategoryVO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SpotCategoryServiceImpl extends ServiceImpl<SpotCategoryMapper, SpotCategory> implements SpotCategoryService {

    private static final int ENABLED_STATUS = 1;

    @Override
    public List<SpotCategoryVO> listEnabledCategories() {
        return lambdaQuery()
                .eq(SpotCategory::getStatus, ENABLED_STATUS)
                .orderByAsc(SpotCategory::getSort)
                .list()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private SpotCategoryVO toVO(SpotCategory category) {
        SpotCategoryVO vo = new SpotCategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSort(category.getSort());
        return vo;
    }
}
