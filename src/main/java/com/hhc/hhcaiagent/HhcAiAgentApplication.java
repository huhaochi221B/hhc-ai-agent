package com.hhc.hhcaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
@MapperScan("com.hhc.hhcaiagent.mapper")
public class HhcAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HhcAiAgentApplication.class, args);
    }

}
