package com.learnmate;

import com.learnmate.ai.service.EmbeddingService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingServiceTest {

    private final EmbeddingService service = new EmbeddingService();

    @Test
    void sameTextHasSimilarityOne() {
        double[] left = service.embed("RAG 的检索流程");
        double[] right = service.embed("RAG 的检索流程");
        assertThat(service.cosine(left, right)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    void relatedTextScoresHigherThanUnrelatedText() {
        double[] query = service.embed("JWT 登录校验");
        double[] related = service.embed("JWT 的签名与过期时间校验");
        double[] unrelated = service.embed("今天食堂的午饭菜单");
        assertThat(service.cosine(query, related))
                .isGreaterThan(service.cosine(query, unrelated));
    }

    @Test
    void csvRoundTripKeepsDimension() {
        double[] vector = service.embed("Spring Boot 数据源");
        double[] restored = service.fromCsv(service.toCsv(vector));
        assertThat(restored).hasSize(EmbeddingService.DIMENSION);
        assertThat(service.cosine(vector, restored)).isCloseTo(1.0,
                org.assertj.core.data.Offset.offset(0.001));
    }
}
