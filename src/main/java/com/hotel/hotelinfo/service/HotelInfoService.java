package com.hotel.hotelinfo.service;

import com.hotel.hotelinfo.dto.HotelInfoDTO;
import com.hotel.hotelinfo.model.KnowledgeDocument;
import com.hotel.knowledge.repository.KnowledgeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelInfoService {

    private KnowledgeRepository knowledgeRepo;

    public HotelInfoService(KnowledgeRepository knowledgeRepo) {
        this.knowledgeRepo = knowledgeRepo;
    }

    public List<HotelInfoDTO> getHotelInfo() {
        return knowledgeRepo.findAll().stream()
                    .map(this::convertHotelInfoToDTO)
                    .toList();

    }
    private HotelInfoDTO convertHotelInfoToDTO(KnowledgeDocument hotelInfo) {
        return new HotelInfoDTO(hotelInfo.getTitle(),
                                hotelInfo.getText(),
                                hotelInfo.getCategory(),
                                hotelInfo.getTags(),
                                hotelInfo.getSource());
    }
}
