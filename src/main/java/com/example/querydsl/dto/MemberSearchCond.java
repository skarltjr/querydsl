package com.example.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCond {
    //회원명 팀명 나이
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
    //화면에서 회원 이름 팀명등 입력한거 받는
}
