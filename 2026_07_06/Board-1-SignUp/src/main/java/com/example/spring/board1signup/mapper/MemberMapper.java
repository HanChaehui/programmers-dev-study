package com.example.spring.board1signup.mapper;

import com.example.spring.board1signup.domain.entity.Member;
import com.example.spring.board1signup.dto.MemberJoinRequestDto;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public Member toEntity(MemberJoinRequestDto dto) {
        return Member.builder()
                .userId(dto.getUserId())
                .password(dto.getPassword())
                .userName(dto.getUserName())
                .build();
    }
}
