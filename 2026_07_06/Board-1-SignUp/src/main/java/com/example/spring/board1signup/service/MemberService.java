package com.example.spring.board1signup.service;

import com.example.spring.board1signup.domain.entity.Member;
import com.example.spring.board1signup.domain.repository.MemberRepository;
import com.example.spring.board1signup.dto.LoginRequestDto;
import com.example.spring.board1signup.dto.MemberJoinRequestDto;
import com.example.spring.board1signup.exception.DuplicateUserIdException;
import com.example.spring.board1signup.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository repository;
    private final MemberMapper memberMapper;

    @Transactional
    public void join(MemberJoinRequestDto dto) {
        if(repository.existsByUserId(dto.getUserId())) {
            throw new DuplicateUserIdException("이미 존재하는 아이디");
        }
        repository.save(memberMapper.toEntity(dto));
    }

    public Optional<Member> login(LoginRequestDto dto) {
        return repository.findByUserId(dto.getUsername())
                .filter( member -> member.getPassword().equals(dto.getPassword()));
    }
}
