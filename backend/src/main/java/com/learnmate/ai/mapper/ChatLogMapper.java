package com.learnmate.ai.mapper;

import com.learnmate.ai.entity.ChatLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatLogMapper {

    int insert(ChatLog log);

    int countToday();
}
