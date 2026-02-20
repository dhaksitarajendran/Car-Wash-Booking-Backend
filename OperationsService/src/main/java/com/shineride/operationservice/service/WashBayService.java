package com.shineride.operationservice.service;

import com.shineride.operationservice.dto.WashBayRequestDTO;
import com.shineride.operationservice.entity.WashBayEntity;

import java.util.List;

public interface WashBayService {

    // ✅ ADMIN
    WashBayEntity createWashBay(WashBayRequestDTO dto);

    // ✅ ADMIN
    List<WashBayEntity> getAllWashBays();

    // ✅ ADMIN
    WashBayEntity disableWashBay(String washbayId);
}