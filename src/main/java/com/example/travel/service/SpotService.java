package com.example.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.travel.dto.NearbySpotQueryDTO;
import com.example.travel.dto.SpotQueryDTO;
import com.example.travel.dto.SpotSaveDTO;
import com.example.travel.entity.Spot;
import com.example.travel.vo.NearbySpotVO;
import com.example.travel.vo.PageResult;
import com.example.travel.vo.SpotDetailVO;
import com.example.travel.vo.SpotListVO;
import java.util.List;

public interface SpotService extends IService<Spot> {

    PageResult<SpotListVO> listSpots(SpotQueryDTO queryDTO);

    SpotDetailVO getSpotDetail(Long id);

    List<SpotListVO> listHotSpots(Integer limit);

    List<NearbySpotVO> queryNearbySpots(NearbySpotQueryDTO queryDTO);

    Long loadSpotGeoData();

    Long createSpot(SpotSaveDTO saveDTO);

    void updateSpot(Long id, SpotSaveDTO saveDTO);
}
