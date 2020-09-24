package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCond;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.awt.print.Pageable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired MemberRepository memberRepository;
    @Test
    public void basicTest()
    {
        Member member=new Member("member1",10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> all = memberRepository.findAll();
        assertThat(all).containsExactly(member);

        List<Member> byName = memberRepository.findByUsername(member.getUsername());
        assertThat(byName).containsExactly(member);
    }

    @Test
    public void searchTest()
    {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCond cond = new MemberSearchCond();
        cond.setAgeGoe(35);
        cond.setAgeLoe(40);
        cond.setTeamName("teamB");
        //만약에 동적쿼리에 조건이 하나도 없으면 ? = 데이터 다 끌고온다 조심.

        //List<MemberTeamDto> result = memberRepository.searchByBuilder(cond);
        List<MemberTeamDto> result2 = memberRepository.search(cond);

        //assertThat(result).extracting("username").containsExactly("member4");
        assertThat(result2).extracting("username").containsExactly("member4");
    }

    @Test
    public void searchPageSimpleTest()
    {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCond cond = new MemberSearchCond();
       /* cond.setAgeGoe(35);
        cond.setAgeLoe(40);
        cond.setTeamName("teamB");*/

        //만약에 동적쿼리에 조건이 하나도 없으면 ? = 데이터 다 끌고온다 조심.
        //그걸 이용해서 지금 예시가4개라 데이터가 너무 적으니 조건은 안넘긴다

        PageRequest request = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(cond, request);
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting("username").containsExactly("member1","member2","member3");
    }
}