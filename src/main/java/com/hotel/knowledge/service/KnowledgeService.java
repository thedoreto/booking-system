package com.hotel.knowledge.service;

import com.hotel.ai.client.GeminiEmbeddingClient;
import com.hotel.hotelinfo.model.KnowledgeDocument;
import com.hotel.knowledge.repository.KnowledgeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
/**
 * @deprecated This client is part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
@Service
public class KnowledgeService {

    private KnowledgeRepository knowledgeRepo;
    private  GeminiEmbeddingClient geminiEmbeddingClient;

    public KnowledgeService(KnowledgeRepository knowledgeRepo,
                            GeminiEmbeddingClient geminiEmbeddingClient) {
        this.knowledgeRepo = knowledgeRepo;
        this.geminiEmbeddingClient = geminiEmbeddingClient;
    }

    public List<KnowledgeDocument> findRelevant(String question) throws Exception {
      //  testKnowledge();
        var embedding = geminiEmbeddingClient.getEmbedding(question);

        var result = knowledgeRepo.searchByVector(embedding);

        System.out.println("Question: " + question);
        System.out.println("Found: " + result.size());

        return result;
    }

    //only testing
    public void testKnowledge() {

        try {
            List<Double> em = geminiEmbeddingClient.getEmbedding("Хотелът има седем(7) звезди.");
            String vectorString = em.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            // Принтираме готовия низ в конзолата
            System.out.println("[" + vectorString + "]");

       /*     List<KnowledgeDocument> results = findRelevant("В колко часа е закуската?");


            results.forEach(doc -> {
                System.out.println(doc.getText());
                // System.out.println(doc.getEmbedding());
            });*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
