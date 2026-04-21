package com.hhc.hhcaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class PgVectorStoreConfigTest {
    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorStore() {
        List<Document> documents = List.of(
                new Document("Spring AI太棒了！！Spring AI太棒了！！Spring AI太棒了！！Spring AI太棒了！！Spring AI太棒了！！", Map.of("meta1", "meta1")),
                new Document("世界广阔，救赎就在转角处潜伏"),
                new Document("你向前走，面对过去，然后转身面向未来.", Map.of("meta2", "meta2")));

        pgVectorVectorStore.add(documents);

        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        Assertions.assertNotNull(results);
    }
}