package com.learnmate.ai.service;

import com.learnmate.ai.config.RagProperties;
import com.learnmate.ai.dto.RetrievedChunk;
import com.learnmate.ai.entity.KnowledgeChunk;
import com.learnmate.ai.mapper.KnowledgeChunkMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private static final List<String> KNOWLEDGE_FILES = List.of(
            "knowledge/rag-basics.md",
            "knowledge/spring-boot.md",
            "knowledge/security.md",
            "knowledge/git-ci.md"
    );

    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingService embeddingService;
    private final RagProperties properties;

    public RagService(KnowledgeChunkMapper chunkMapper,
                      EmbeddingService embeddingService,
                      RagProperties properties) {
        this.chunkMapper = chunkMapper;
        this.embeddingService = embeddingService;
        this.properties = properties;
    }

    @Transactional
    public int ingestIfEmpty() {
        if (chunkMapper.countAll() > 0) {
            return 0;
        }
        int count = 0;
        for (String file : KNOWLEDGE_FILES) {
            count += ingest(file);
        }
        log.info("RAG knowledge base initialized with {} chunks", count);
        return count;
    }

    private int ingest(String classpathFile) {
        ClassPathResource resource = new ClassPathResource(classpathFile);
        if (!resource.exists()) {
            log.warn("Knowledge file not found: {}", classpathFile);
            return 0;
        }
        try {
            String markdown = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            List<KnowledgeChunk> chunks = parseMarkdown(markdown);
            for (KnowledgeChunk chunk : chunks) {
                double[] vector = embeddingService.embed(
                        chunk.getDocTitle() + " " + chunk.getSection() + " " + chunk.getContent());
                chunk.setEmbedding(embeddingService.toCsv(vector));
                chunkMapper.insert(chunk);
            }
            return chunks.size();
        } catch (IOException ex) {
            throw new IllegalStateException("读取知识库失败: " + classpathFile, ex);
        }
    }

    List<KnowledgeChunk> parseMarkdown(String markdown) {
        List<KnowledgeChunk> result = new ArrayList<>();
        String docTitle = "LearnMate 课程资料";
        String section = "概述";
        StringBuilder body = new StringBuilder();
        for (String rawLine : markdown.split("\\R")) {
            String line = rawLine.trim();
            if (line.startsWith("# ") && !line.startsWith("## ")) {
                docTitle = line.substring(2).trim();
                flush(docTitle, section, body, result);
            } else if (line.startsWith("## ")) {
                flush(docTitle, section, body, result);
                section = line.substring(3).trim();
            } else if (line.startsWith("### ")) {
                flush(docTitle, section, body, result);
                section = section + " / " + line.substring(4).trim();
            } else {
                body.append(rawLine).append('\n');
            }
        }
        flush(docTitle, section, body, result);
        return result;
    }

    private void flush(String docTitle, String section, StringBuilder body, List<KnowledgeChunk> result) {
        String text = body.toString().trim();
        body.setLength(0);
        if (text.isEmpty()) {
            return;
        }
        List<String> chunks = splitByParagraph(text, properties.chunkSize());
        for (String content : chunks) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocTitle(docTitle);
            chunk.setSection(section);
            chunk.setContent(content);
            result.add(chunk);
        }
    }

    private List<String> splitByParagraph(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : text.split("\\n\\s*\\n")) {
            String cleaned = paragraph.trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            if (current.length() > 0 && current.length() + cleaned.length() > maxLength) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
            if (cleaned.length() > maxLength) {
                for (int start = 0; start < cleaned.length(); start += maxLength) {
                    chunks.add(cleaned.substring(start, Math.min(cleaned.length(), start + maxLength)));
                }
            } else {
                current.append(cleaned).append("\n\n");
            }
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    public List<RetrievedChunk> search(String question) {
        double[] query = embeddingService.embed(question);
        List<RetrievedChunk> scored = new ArrayList<>();
        for (KnowledgeChunk chunk : chunkMapper.findAll()) {
            double semantic = embeddingService.cosine(query, embeddingService.fromCsv(chunk.getEmbedding()));
            // 标题或章节命中时加权，改善排序
            double sectionHit = embeddingService.overlap(question,
                    chunk.getDocTitle() + " " + chunk.getSection());
            double score = Math.min(1.0, semantic + 0.9 * sectionHit);
            if (score >= properties.minScore()) {
                scored.add(new RetrievedChunk(chunk.getId(), chunk.getDocTitle(),
                        chunk.getSection(), chunk.getContent(), score));
            }
        }
        scored.sort(Comparator.comparingDouble(RetrievedChunk::score).reversed());
        return scored.stream().limit(properties.topK()).toList();
    }

    public String buildContext(List<RetrievedChunk> chunks) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk chunk = chunks.get(i);
            builder.append('[').append(i + 1).append("] 《")
                    .append(chunk.docTitle()).append("》 · ")
                    .append(chunk.section()).append('\n')
                    .append(chunk.content()).append("\n\n");
        }
        return builder.toString().trim();
    }
}
