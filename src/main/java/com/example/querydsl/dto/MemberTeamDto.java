package com.example.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberTeamDto {
    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teanName;

    // @QueryProjection  dto가 순순했을 때 좋다 그래서 난 projection.constructor사용한다
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teanName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teanName = teanName;
    }

    //gradle로 dsl 컴파일 다시해줘야한다
}
