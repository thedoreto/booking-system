package com.hotel.hotelinfo.repository;

import com.hotel.hotelinfo.model.KnowledgeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelInfoRepository extends MongoRepository<KnowledgeDocument, String> {

  //  Optional<KnowledgeDocument> findByName(String name);
}
