package com.example.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.travel.entity.SpotCategory;
import com.example.travel.vo.SpotCategoryVO;
import java.util.List;

public interface SpotCategoryService extends IService<SpotCategory> {

    List<SpotCategoryVO> listEnabledCategories();
}
