package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCond;
import com.example.querydsl.dto.MemberTeamDto;

import com.example.querydsl.entity.Member;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
public class MemberJpaRepository {
    private EntityManager em;
    private JPAQueryFactory queryFactory;

    @Autowired
    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        queryFactory =new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll()
    {
        return em.createQuery("select m from Member m",Member.class).getResultList();
    }
// 비교
    public List<Member> findAll_querydsl()
    {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByName(String name)
    {
        return em.createQuery("select m from Member m where m.username =:name")
                .setParameter("name",name)
                .getResultList();
    }

    //비교
    public List<Member> findByName_querydsl(String name) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(name))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCond cond)
    {
        BooleanBuilder builder =new BooleanBuilder();
        if(hasText(cond.getUsername()))   //화면에서 null이나 ""가 들어오는 경우가 많다 그래서 스프링프레임웤에 스트링유틸스사용
        {
            builder.and(member.username.eq(cond.getUsername()));
        }
        if (hasText(cond.getTeamName()))
        {
            builder.and(team.name.eq(cond.getTeamName()));
        }
        if (cond.getAgeGoe() != null) {
            builder.and(member.age.goe(cond.getAgeGoe()));
        }
        if (cond.getAgeLoe() != null) {
            builder.and(member.age.loe(cond.getAgeLoe()));
        }


        return queryFactory
                .select(Projections.constructor(MemberTeamDto.class,
                        member.id,member.username,member.age,team.id,team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> search(MemberSearchCond cond) {
        return queryFactory
                .select(Projections.constructor(MemberTeamDto.class,
                        member.id,member.username,member.age,team.id,team.name))
                .from(member)
                .leftJoin(member.team,team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                )
                .fetch();
    }
    private Predicate usernameEq(String username) {
        return hasText(username) ? member.username.eq(username):null ;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName):null ;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) :null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) :null;
    }
}
