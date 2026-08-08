package com.hotel.hotelinfo.service;

import com.hotel.hotelinfo.dto.HotelInfoDTO;
import com.hotel.hotelinfo.dto.KnowledgeDTO;
import com.hotel.hotelinfo.model.HotelInfo;
import com.hotel.hotelinfo.model.KnowledgeDocument;
import com.hotel.hotelinfo.repository.HotelInfoRepository;
import com.hotel.knowledge.repository.KnowledgeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelInfoService {

    private final KnowledgeRepository knowledgeRepo;
    private final HotelInfoRepository hotelInfoRepo;

    public HotelInfoService(KnowledgeRepository knowledgeRepo,
                            HotelInfoRepository hotelInfoRepo) {
        this.knowledgeRepo = knowledgeRepo;
        this.hotelInfoRepo = hotelInfoRepo;
    }

    public List<HotelInfoDTO> getHotelInfo() {

        List<HotelInfo> hotelInfoList = hotelInfoRepo.findAll();
        List<KnowledgeDocument> knowledgeList = knowledgeRepo.findAll();

        return hotelInfoList.stream()
                .map(hotelInfo -> convertToDTO(hotelInfo, knowledgeList))
                .toList();
    }

    private HotelInfoDTO convertToDTO(
            HotelInfo hotelInfo,
            List<KnowledgeDocument> knowledgeList) {

        List<KnowledgeDTO> knowledgeDTOs = knowledgeList.stream()
                .map(knowledge -> new KnowledgeDTO(
                        knowledge.getTitle(),
                        knowledge.getText(),
                        knowledge.getCategory(),
                        knowledge.getTags(),
                        knowledge.getSource()
                ))
                .toList();

        return new HotelInfoDTO(
                knowledgeDTOs,
                hotelInfo.getName(),
                hotelInfo.getDescription(),
                hotelInfo.getAddress(),
                hotelInfo.getPhone(),
                hotelInfo.getEmail(),
                hotelInfo.getImageIds()
        );
    }
}