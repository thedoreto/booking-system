package com.hotel.hotelinfo.repository;

import com.hotel.hotelinfo.model.HotelInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelInfoRepository extends MongoRepository<HotelInfo, String> {

  //  Optional<KnowledgeDocument> findByName(String name);
}
