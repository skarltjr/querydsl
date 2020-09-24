package com.example.querydsl.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString(of={"id","username","age"})
public class Member{
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;


    public Member(String name, int age) {
        this.username=name;
        this.age=age;
    }
    public Member(String username) {
        this.username = username;
    }

    protected Member() {
    }   //jpa 기본생성자도 있어야하지만 아무곳에서나 호출되는건 x

    public Member(String name, int i, Team teamA) {
        this.username=name;
        this.age=i;
        if (teamA != null) {
            changeTeam(teamA);
        }
    }
    //디폴트생성자는 jpa의 표준스펙 = 프록시쓸 때 필요

    public void changeUsername(String username) {
        this.username = username;
    }

    //연관관계 편의
    public void changeTeam(Team team) {
        this.team=team;
        team.getMembers().add(this);
    }
}