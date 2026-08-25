package com.hotel.hotelinfo.service;

import com.hotel.hotelinfo.dto.HotelInfoDTO;
import com.hotel.hotelinfo.model.HotelInfo;
import com.hotel.hotelinfo.repository.HotelInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelInfoService {

    private final HotelInfoRepository hotelInfoRepo;

    public HotelInfoService(HotelInfoRepository hotelInfoRepo) {
        this.hotelInfoRepo = hotelInfoRepo;
    }

    public List<HotelInfoDTO> getHotelInfo() {

        List<HotelInfo> hotelInfoList = hotelInfoRepo.findAll();

        return hotelInfoList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private HotelInfoDTO convertToDTO(HotelInfo hotelInfo) {

        return new HotelInfoDTO(
                hotelInfo.getName(),
                hotelInfo.getDescription(),
                hotelInfo.getAddress(),
                hotelInfo.getPhone(),
                hotelInfo.getEmail(),
                hotelInfo.getImageIds()
        );
    }
}