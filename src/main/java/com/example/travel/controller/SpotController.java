package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.NearbySpotQueryDTO;
import com.example.travel.dto.SpotQueryDTO;
import com.example.travel.dto.SpotSaveDTO;
import com.example.travel.service.SpotCategoryService;
import com.example.travel.service.SpotService;
import com.example.travel.vo.NearbySpotVO;
import com.example.travel.vo.PageResult;
import com.example.travel.vo.SpotCategoryVO;
import com.example.travel.vo.SpotDetailVO;
import com.example.travel.vo.SpotListVO;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SpotController {

    private final SpotCategoryService spotCategoryService;

    private final SpotService spotService;

    @GetMapping("/spot/category/list")
    public Result<List<SpotCategoryVO>> listCategories() {
        return Result.success(spotCategoryService.listEnabledCategories());
    }

    @GetMapping("/spot/list")
    public Result<PageResult<SpotListVO>> listSpots(SpotQueryDTO queryDTO) {
        return Result.success(spotService.listSpots(queryDTO));
    }

    @GetMapping("/spot/hot")
    public Result<List<SpotListVO>> listHotSpots(@RequestParam(required = false) Integer limit) {
        return Result.success(spotService.listHotSpots(limit));
    }

    @GetMapping("/spot/nearby")
    public Result<List<NearbySpotVO>> listNearbySpots(NearbySpotQueryDTO queryDTO) {
        return Result.success(spotService.queryNearbySpots(queryDTO));
    }

    @GetMapping("/spot/{id}")
    public Result<SpotDetailVO> getSpotDetail(@PathVariable Long id) {
        return Result.success(spotService.getSpotDetail(id));
    }

    @PostMapping("/admin/spot")
    public Result<Long> createSpot(@RequestBody @Valid SpotSaveDTO saveDTO) {
        return Result.success(spotService.createSpot(saveDTO));
    }

    @PutMapping("/admin/spot/{id}")
    public Result<Void> updateSpot(@PathVariable Long id, @RequestBody @Valid SpotSaveDTO saveDTO) {
        spotService.updateSpot(id, saveDTO);
        return Result.success();
    }

    @PostMapping("/admin/spot/geo/load")
    public Result<Long> loadSpotGeoData() {
        return Result.success(spotService.loadSpotGeoData());
    }
}
