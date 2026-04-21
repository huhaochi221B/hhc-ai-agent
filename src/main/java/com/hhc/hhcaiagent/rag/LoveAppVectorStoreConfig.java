package com.hhc.hhcaiagent.rag;


import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 3.向量转换和存储
 * 向量配置
 */
@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;
    @Bean
    VectorStore loveAppVectorStore(@Qualifier("dashscopeEmbeddingModel")EmbeddingModel dashScopeEmbeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel)
                .build();
        //加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        //自主切分
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
        //自动补充关键词元信息
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
