package com.example.querydsl;

import com.example.querydsl.dto.MemberDto;
import com.example.querydsl.dto.QMemberDto;
import com.example.querydsl.dto.userDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.QTeam;
import com.example.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.example.querydsl.entity.QMember.*;
import static com.example.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;

    @BeforeEach
    public void before()
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

    }

    @Test
    public void startJPQL()
    {
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        //지금 Qmember없는데 그레이들에서 task other compilequerydsl해주면 된다 그럼 빌드에 generated에 만들어진다.
        QMember mem = new QMember("m");  //"m" 은 이름을 주는거
        Member findMember = queryFactory
                .select(mem)
                .from(mem)
                .where(mem.username.eq("member1")) //파라미터 바인딩도 필요없다
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    @Test
    public void startQuerydsl1()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        //QMember mem = new QMember("m");   만약 같은테이블을 서로 조인해야할 때 이름이 달라야하니까 그 때만 선언해서쓰고 ★
        //위처럼 이렇게 사용하는게 가장 기본이지만
        //QMember member = QMember.member; 가 이미 구현되어있다 내부적으로 근데 더 줄여서

        Member findMember = queryFactory
               // .select(QMember.member) 처럼 바로 쓸 수 있는데 이걸 static import해서
                .select(member)  //이렇게 까지 만들 수 있고 가장 깔끔하다 연습
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    @Test
    public void search()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        Member findMember = queryFactory
                .selectFrom(member) // 합쳐도된다
                .where(member.username.eq("member1").and(member.age.eq(10)))  //당연히 or도 다 가능
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");


        /**
         * 다양한 검색조건 예시
         * member.username.eq("member1") // username = 'member1'
         * member.username.ne("member1") //username != 'member1'
         * member.username.eq("member1").not() // username != 'member1'
         * member.username.isNotNull() //이름이 is not null
         * member.age.in(10, 20) // age in (10,20)  나이가 10살 이거나 20살인사람
         * member.age.notIn(10, 20) // age not in (10, 20)
         * member.age.between(10,30) //between 10, 30
         * member.age.goe(30) // age >= 30  ★
         * member.age.gt(30) // age > 30   ★
         * member.age.loe(30) // age <= 30
         * member.age.lt(30) // age < 30
         * member.username.like("member%") //like 검색
         * member.username.contains("member") // like ‘%member%’ 검색
         * member.username.startsWith("member") //like ‘member%’ 검색
         * */
    }
    @Test
    public void searchAndParam()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> members = queryFactory
                .selectFrom(member) // 합쳐도된다
                .where(
                       /* member.username.eq("member1"),
                        member.age.eq(10) //and 경우 그냥 , 하나만 사용해도된다
                        .or(member.age.eq(20))*/
                        (member.username.eq("member1").and (member.age.eq(10)))
                                .or(member.age.eq(20))
                )
                .fetch();
        /*Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");*/
        Assertions.assertThat(members.size()).isEqualTo(2);
        Assertions.assertThat(members.get(1).getAge()).isEqualTo(20);
    }

    @Test
    public void resultFetch()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member one = queryFactory
                .selectFrom(member)
                .fetchOne();

        //
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        results.getTotal();
        List<Member> content = results.getResults();
        //result로 페이징도 가능
        //여기는 쿼리가 2방나간다 왜냐 total카운트도 가져오기 때문

        long count = queryFactory
                .selectFrom(member)
                .fetchCount();
        //카운트만 할 수 있다.

    }

    /**
     * 회원정렬 순서
     * 1.나이 내림차순
     * 2.이름 올림차순
     * 단 2에서 회원이름이 없으면 마지막에 출력 nulls last
     * */
    @Test
    public void sort()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));  //기존 데이터에 추가

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = members.get(0);
        Member member6 = members.get(1);
        Member nullMember = members.get(2);

        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(nullMember.getUsername()).isNull();

    }

    @Test
    public void paging()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> list = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)  //이러면 0번은 제외하고 1부터
                .limit(2)
                .fetch();

        Assertions.assertThat(list.size()).isEqualTo(2);
    }

    @Test
    public void paging2()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)  //이러면 0번은 제외하고 1부터
                .limit(2)
                .fetchResults();// 페이징은 fetchresults


        Assertions.assertThat(results.getTotal()).isEqualTo(4);
        Assertions.assertThat(results.getOffset()).isEqualTo(1);
        Assertions.assertThat(results.getLimit()).isEqualTo(2);
        Assertions.assertThat(results.getResults().size()).isEqualTo(2);

        Member member = results.getResults().get(0);
        Assertions.assertThat(member.getUsername()).isEqualTo("member3");  //offset1 이니까
        //getResults 하면 컨텐트가 나오니까

    }

    @Test //집합
    public void aggregation()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
        //이렇게하면 모든 회원에대한 카운트 //회원들 나이합 // 나이 평균 // 최고 최소값
        /**  튜플로 저장된다    이건 참고로 쿼리dsl의 튜플*/
        Tuple tuple = result.get(0);
        Assertions.assertThat(tuple.get(member.count())).isEqualTo(4); // 기본 데이터 4개넣어놨으니까
        Assertions.assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        Assertions.assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        Assertions.assertThat(tuple.get(member.age.min())).isEqualTo(10);


        //실제로는 dto롤 뽑아오는걸 더 많이 사용
    }

    /** 팀의 이름과 각 팀의 평균 연령을 구해라*/
    @Test
    public void group() throws Exception
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg()) // QTeam.team.name static import
                .from(member)
                .join(member.team, team)  //member의 팀을 team과 조인
                .groupBy(team.name)//team이름으로 그룹을만든다
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        Assertions.assertThat(teamA.get(team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        Assertions.assertThat(teamB.get(team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * 팀A에 소속된 모든 member  조인을통해
     * */
    @Test
    public void join()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Member> members = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        Assertions.assertThat(members)
                .extracting("username")
                .containsExactly("member1","member2");
    }

    /**
     * 연관관계가 없어도 조인이 가능하다
     * 사람이름이 teamA teamB처럼 팀이름과 동일한 사람찾기 = 팀 멤버 테이블 둘다필요    . 막  조인
     * 근데 외부조인 left(outer) 조인이 안되는데 on절로 가능하도록(where대신)
     * */
    @Test
    public void theta_join()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        em.persist(new Member("teamA"));  //이 친구들은 team과의 연관관계가 없다 이것들도 조인할 수 있다
        em.persist(new Member("teamB"));




        /**
         * 헷갈려하는 부분이 어디서는 select member ,select member,team 으로다르고
         * from member ,from member,team으로 다 다른데   생각해보면
         * selelct member는 말 그대로 멤버내용만 member,team은 [Member(id=4, username=member2, age=20), Team(id=1, name=teamA)]
         * 처럼 멤버, 팀 내용 둘 다 튜플로
         * */




        List<Member> members = queryFactory
                .select(member)
                .from(member, team)  //당연히 멤버이름이랑 팀 이름 둘다 알려면 from member,team이지
     //말 그대로 전혀 연관이 없다. 멤버들의 변수 team 중에서가 아니라 그냥 팀 테이블 싹 다 가져와서 그 중에서
     //멤버 전체 중 이름이 팀 전체 중의 아무 팀의 이름이랑 같은 이름을 가진 멤버만
                .where(member.username.eq(team.name))
                .fetch();
        //그러면 회원  팀 데이터 다 가져오고 where로 필터링한 결과
        for (Member member1 : members) {
            System.out.println(member1);
        }
    }

    //on 절  은  조건 달때나 아니면 정말 연관관계 없는 애들조인할 때
    /**
     * 회원과 팀을 조인하되 팀 이름이 teamA인 팀만 조인해라  그리고 회원은 모두 조회
     * JPQL : select m,t from Member m left join m.team t on t.name="teamA"
     * */
    @Test
    public void join_on()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory  //select 가 여러 타입이니까 튜플로나옴
                .select(member, team)   //멤버 팀 테이블 싹다 가져와라
                .from(member)  //왜 member만이냐 ? => 팀전체가 필요한게 아니라 멤버와 연관관계를 가진 팀 중 이름이 teamA인애들만
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                //여기서 만약에 이걸 그냥 join으로 바꾸면 팀A만 가져왔으니 나머지는 teamB를 가진
                // 멤버들은 팀이 null이라 다 빠지고 teamA인애들만 나온다 그게 join  / left join 차이다
                //그리고 left조인같은 외부조인이 아니라 그냥 내부 join이면 on말고 where로 조건문쓰면된다
                //반대로 말하면 외부조인은 where이 아니라 on으로 조건걸어서 가져와야한다
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = "+tuple);
        }
       /**
        * left join처럼 외부조인을 사용하면   멤버이름   나이  teamA
        *                                  멤버이름  나이  null    // on절로 팀A만 가져왔으니 teamB를 가진것들은 팀이 없다
        *
        * 외부조인이 아닌 내부 그냥 join     멤버이름    나이 teamA
        *                                없음. 팀이 없는 건 그냥 없음 데이터
        * */
    }

    /**
     * 연관관계가 없어도 조인이 가능하다
     * */
    @Test
    public void join_on_noRelation()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team) //멤버랑 팀 내용 둘다 출력하겠다 대신 팀은 전체 팀이아니라 on절에 해당하는하며 멤버와 연관관계가 있는 애들 중
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))   //원래 leftjoin member.team ,team이랑 문법도 다름
                //이 경우에도 join으로 바꾸면 team이름이랑 이름이 같은 멤버만 나오고
                //leftjoin이면 싹 다 가져와도 team이랑 이름 안맞는 것도보여준다
                .fetch();           //정말 막 조인
        for (Tuple tuple : result) {
            System.out.println("tuple + " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoin()
    {
        em.flush();
        em.clear();
        //영속성컨텍스트 비워준 후
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        Member result = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(result.getTeam());// 이게 로딩이 돼었는지 true /false
        Assertions.assertThat(loaded).as("패치조인 적용").isTrue();
    }

    /**
     * 쿼리속에 쿼리 서브쿼리    - 조심해야할 부분은 alias가 겹치면안된다
     * 나이가 가장 많은 회원
     * */
    @Test
    public void subQuery()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember memberSub = new QMember("memberSub");   //조심해야할 부분은 alias가 겹치면안된다

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(       //사실상 아래 서브쿼리를 통해 eq(40) 이랑 같은 결과
                        JPAExpressions   //서브쿼리를 하기 위해 필수
                                .select(memberSub.age.max())
                                .from(memberSub) //alias만 다른거지 결국엔 from member
                )).fetch();
        Assertions.assertThat(members).extracting("age")   //멤버스에서 나이 필드를 가져오고
                                        .containsExactly(40);  //이게 정확하게 40살
    }

    /**
     * 나이가 평균이상인 회원
     * */
    @Test
    public void subQuery2()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember memberSub = new QMember("memberSub");   //조심해야할 부분은 alias가 겹치면안된다

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                )).fetch();
    }

    /**
     * 서브쿼리에서 in 많이쓴다
     * */
    @Test
    public void subQuery_in()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember memberSub = new QMember("memberSub");   //조심해야할 부분은 alias가 겹치면안된다

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.in(   //그러면 나이가 10살보다 많은 20 30 40살에서 member.age를 가져온디
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                )).fetch();
        Assertions.assertThat(members).extracting("age")
                .containsExactly(20,30,40);
    }

    //where말고 select 에서도 서브쿼리가능하다
    @Test
    public void select_subQuery()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember memberSub = new QMember("memberSub");
        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions  //static import할 수도있다.
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }


    /**         서브쿼리의 한계는 from 절에서는 지원이 안된다 ★     보통 join으로 바꾸거나 2번에 걸쳐서 쿼리쓰는 방법으로 대체할 수 있다  */

    @Test
    public void simpleCase()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        List<String> list = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : list) {
            System.out.println(s);
        }
    }

    @Test
    public void complexCase()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        List<String> list = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30")
                        .otherwise("기타"))
                .from(member)
                .fetch();
    }

    @Test
    public void constant()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> list = queryFactory
                .select(member.username, Expressions.constant("A"))
                .fetch();
       //그냥 예를들어 원래대로라면 멤버이름만 나올텐데 거기다가  [멤버이름, A] 로 다 나온다 그래서 튜플이되는거고
        for (Tuple tuple : list) {
            System.out.println(tuple);
        };
    }

    @Test
    public void concat()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        //{username}_{age} 처럼 다 뽑아내고싶어서
        List<String> fetch = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))//나이는 string이아니니까 변환
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : fetch) {
            System.out.println(s);
        }
    }

    // 나중에 enum 출력하고싶을 때 stringValue() 자주쓴다

    @Test
    public void tupleProjection()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<Tuple> tuples = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : tuples) {
            //튜플 사용하는 방법
            System.out.println(tuple.get(member.username)); //그냥 이렇게 뽑아쓰면된다
            Integer integer = tuple.get(member.age);
            //반환 타입도 다 잡아준다
        }
    }

    @Test
    public void findDtoJPQL()
    {
        List<MemberDto> resultList = em.createQuery("select new com.example.querydsl.dto.MemberDto(m.username,m.age) from Member m",
                MemberDto.class).getResultList();
        //JPQL로 가져오던 방버
    }
    @Test
    public void findDto()
    {
        // 쿼리dsl  setter(@Data의)이용해서  dto변환
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<MemberDto> dtos = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto dto : dtos) {
            System.out.println(dto);
        }
    }

    @Test
    public void findDto2()
    {
        // @Data의 getter setter조차 없어도된다
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<MemberDto> dtos = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto dto : dtos) {
            System.out.println(dto);
        }
    }

    @Test
    public void findDto2_1()
    {
        QMember memberSub = new QMember("memberSub");
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<userDto> dtos = queryFactory
                .select(Projections.fields(userDto.class,
                        member.username.as("name"), //fields로 들어가는경우 userDto는 username이아니라 name로 갖는다//그래서 매칭이 안되서 결과출력해보면 null로 나옴 as로 맞춰준것

       //나이는 모두 최고령으로 맞추고싶을 때 서브쿼리로 max찾아오되 이것도 이름을 맞춰줘야해서 Expressions.as로 ,"age"를 추가
                        Expressions.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();
        for (userDto dto : dtos) {
            System.out.println(dto);
        }
    }

    @Test
    public void findDto3()
    {
        // 생성자방식은 위처럼 username 이랑 name 이 다르더라도 어차피 string 이라는 타입만 보기때문에 좋다
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<MemberDto> dtos = queryFactory
                .select(Projections.constructor(MemberDto.class,  //당연히 이건 dto생성자 파라미터 맞춰야함
                        member.username,
                        member.age))
                .from(member)
                .fetch();
       /* queryFactory
                .select(Projections.constructor(member))  //여기서 member가 qmember라 안된다
                .from(member)
                .fetch()*/
        for (MemberDto dto : dtos) {
            System.out.println(dto);
        }
    }
        // 이런 생성자프로젝션의 치명적단점이 컴파일 오류를 못잡는다 그래서 더 편리하고 좋은게 아래
        //다만 아래경우 dto가 쿼리dsl에 의존하게되어서 만약 나중에 쿼리dsl을 다른걸로 갈아끼울 때 문제가 생길 수 있다
    //그래서 상황에 맞게 필요하다싶으면 사용하고 아니면 위 생성자 프로젝션
    @Test
    public void findDtoQueryProjection()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))  //memberDto에서 생성자 @QueryProjection만추가하면
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    /**          distinct는 그냥 select 뒤에 .distinct()하면된다 */

    //동적쿼리
    @Test
    public void dynamicQuery_BooleanBuilder()
    {
        String usernameParam="member1";
        Integer ageParam=10;

        List<Member> result= searchMember1(usernameParam,ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        //동적쿼리 즉 유저네임이나 나이가 파라미터로 들어올텐데 만약에 이름이 null이거나 나이가 null이거나
        //둘다 null인경우가 있을것이고 그 때마다 쿼리가 작동하는 방식을 다르게 해줘야한다면 동적쿼리가 필요하다
        //그럴때 사용

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        BooleanBuilder builder=new BooleanBuilder();
        //만약 반드시 이름은 있어야한다면 new BooleanBuilder(member.username.eq(usernameCond));로 무조건 들어가게 하거나
        if(usernameCond != null)
        {
            builder.and(member.username.eq(usernameCond));
        }
        if(ageCond != null)
        {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)   // ★  그럼 위에서 List<Member> result= searchMember1(usernameParam,ageParam);
                .fetch();       //파라미터 다 채워졌으니 쿼리가파라미터랑 같은 이름,나이가진 멤버 찾아올것
    }

    /** 파라미터로 넘어온 이름나이가 모두같은 애들만 dto로 변환시켜서내보내기  동적쿼리로 */
    @Test
    public void dynamicQuery_BooleanBuilder2()
    {
        String usernameParam="member1";
        Integer ageParam=10;

        List<MemberDto> dynamic = dynamic(usernameParam, ageParam);
        for (MemberDto memberDto : dynamic) {
            System.out.println(memberDto);
        }
    }
    private List<MemberDto> dynamic(String memberCond,Integer ageCond)
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        BooleanBuilder builder =new BooleanBuilder();

        if(memberCond != null && ageCond != null)
        {
            builder.and(member.username.eq(memberCond)).and(member.age.eq(ageCond));
        }
       
        return queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,member.age))
                .from(member)
                .where(builder)
                .fetch();
    }
    ////

    //동적쿼리를 해결하는 방법 2 where문에 다중파라미터를 사용
    //코드도 깔끔해서 잘 익히기
    @Test
    public void dynamicQuery_whereParam()
    {
        String usernameParam="member1";
        Integer ageParam=10;

        List<Member> members = searchMember2(usernameParam, ageParam);

    }
    private List<Member> searchMember2(String usernameCond,Integer ageCond)
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameCond,ageCond)) //null이 들어오면 무시한다 그래서 동적쿼리라는 의미에 부합
                .fetch();
        //이렇게 하면 쿼리가 깔끔하다 booleanbuilder보다
    }

    private BooleanExpression usernameEq(String usernameCond)  /** Predicate타입은 boolean으로도 가능하다 조립할려면 boolean*/
    {
    /*    if(usernameCond!=null)
        {
            return member.username.eq(usernameCond);
        }else{return null;}*/
        //간단하게
        return usernameCond !=null ? member.username.eq(usernameCond):null ;
    }

    private BooleanExpression ageEq(Integer ageCond) {

        return ageCond != null ? member.age.eq(ageCond) : null;
    }
    
    private BooleanExpression allEq(String usernameCond,Integer ageCond) /**이렇게 불리언익스프레션 조립으로 더 간결하게해서 메인쿼리에서사용해도*/
    {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }
    //여러 매서드 만든다고 귀찮은게 아니라 간결하고 보기편하게 만들 수 있다.

    @Test
    public void dynamicQuery_whereParam2()
    {
        String usernameParam="abracadabra";
        Integer ageParam=10;

        List<MemberDto> result= searchMember3(usernameParam,ageParam);
        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    private List<MemberDto> searchMember3(String usernameParam, Integer ageParam) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        return queryFactory
                .select(Projections.constructor(MemberDto.class,member.username,member.age))
                .from(member)
                .where(nameEq(usernameParam).or(ageEqwith(ageParam)))
                .fetch();
    }

    private BooleanExpression nameEq(String usernameParam) {
        return usernameParam == null ? null : member.username.eq(usernameParam);
    }

    private BooleanExpression ageEqwith(Integer ageParam) {
        return ageParam == null ? null : member.age.eq(ageParam);
    }


    ////
    @Test
   // @Commit //db에서 바뀐거 확인하기위해서 사용 test하고 rollback되는거 막기위해잠깐 사용
    public void bulkUpdate()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        long execute = queryFactory
                .update(member)
                .set(member.username, "이름을 바꿔버리기~")
                .where(member.age.lt(28))
                .execute();
        //몇개가 업데이트되었는지

        em.flush();
        em.clear();  //벌크연산하면 항상

        Assertions.assertThat(execute).isEqualTo(2);
    }
    /** 벌크연산의 포인트는 항상 영속성컨텍스트 . db에서 벌크연산 업데이트해도 생각해보면 em.clear안해놓으면
     * 값을 다시가져올때 당연히 영속성컨텍스트에서 먼저 찾는다  그럼 당연히 영속성컨텍스트에는 벌크하기전 값이 저장되어있으니문제*/

    @Test
    public void bulkAdd()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        long execute = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))  //모든 회원의 나이 +1  빼고시피으면 그냥 add(-1)곱은 add대신multiply
                .execute();
    }
    //delete
    @Test
    public void deleteMem()
    {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        long execute = queryFactory
               .delete(member)
                .where(member.age.gt(19))
                .execute();
    }



}
