package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCond;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    EntityManager em;
    @Autowired MemberJpaRepository jpaRepository;


    @Test
    public void basicTest()
    {
        Member member=new Member("member1",10);
        jpaRepository.save(member);

        Member findMember = jpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> all = jpaRepository.findAll_querydsl();
        assertThat(all).containsExactly(member);

        List<Member> byName = jpaRepository.findByName_querydsl(member.getUsername());
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

        List<MemberTeamDto> result = jpaRepository.searchByBuilder(cond);
        List<MemberTeamDto> result2 = jpaRepository.search(cond);

        assertThat(result).extracting("username").containsExactly("member4");
        assertThat(result2).extracting("username").containsExactly("member4");
    }


}