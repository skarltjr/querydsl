package com.example.querydsl.dto;

import com.example.querydsl.entity.Member;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberDto {
    private String username;
    private int age;

    @QueryProjection  // 다음 그레이들에서 querydsl 컴파일 해줘야한다 일단 -> dto도 qmember처럼 qfile로생성이된다
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
    public MemberDto(int age)
    {
        this.age=age;
    }
    public MemberDto(Member member)
    {
        username=member.getUsername();
        age=member.getAge();
    }
    public MemberDto( ) { }
}
