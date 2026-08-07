package com.hotel.hotelinfo.controller;

import com.hotel.hotelinfo.dto.HotelInfoDTO;
import com.hotel.hotelinfo.service.HotelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class HotelInfoController {

    private final HotelInfoService hotelInfoService;

    public HotelInfoController(HotelInfoService hotelInfoService) {
        this.hotelInfoService = hotelInfoService;
    }

    @GetMapping("/hotelinfo")
    public ResponseEntity<List<HotelInfoDTO>> getHotelInfo() {
        return ResponseEntity.ok(hotelInfoService.getHotelInfo());
    }

}
