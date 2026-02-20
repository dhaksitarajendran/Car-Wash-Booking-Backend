package com.shineride.operationservice.service.impl;

import com.shineride.operationservice.dto.WashBayRequestDTO;
import com.shineride.operationservice.entity.WashBayEntity;
import com.shineride.operationservice.repository.WashBayRepository;
import com.shineride.operationservice.service.WashBayService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WashBayServiceImpl implements WashBayService {

    private final WashBayRepository washBayRepository;

    public WashBayServiceImpl(WashBayRepository washBayRepository) {
        this.washBayRepository = washBayRepository;
    }

    @Override
    public WashBayEntity createWashBay(WashBayRequestDTO dto) {
        if (dto == null) throw new RuntimeException("Body required");
        if (dto.getWashbayName() == null || dto.getWashbayName().trim().isEmpty())
            throw new RuntimeException("washbayName required");
        if (dto.getCapacity() <= 0) throw new RuntimeException("capacity must be > 0");

        WashBayEntity bay = new WashBayEntity();
        bay.setId(null);
        bay.setWashbayName(dto.getWashbayName().trim());
        bay.setCapacity(dto.getCapacity());
        bay.setActive(true);

        return washBayRepository.save(bay);
    }

    @Override
    public List<WashBayEntity> getAllWashBays() {
        return washBayRepository.findAll();
    }

    @Override
    public WashBayEntity disableWashBay(String washbayId) {
        WashBayEntity bay = washBayRepository.findById(washbayId)
                .orElseThrow(() -> new RuntimeException("WashBay not found: " + washbayId));
        bay.setActive(false);
        return washBayRepository.save(bay);
    }
}