package com.learnmate.ai.mapper;

import com.learnmate.ai.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper {

    int countAll();

    int insert(KnowledgeChunk chunk);

    List<KnowledgeChunk> findAll();
}
