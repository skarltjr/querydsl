package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCond;
import com.example.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCond cond);
    //동적쿼리같은 것들은 어찌되었든 직접 쿼리 구현해야한다
    //memberRepository는 인터페이스이기 때문에 따로 커스텀으로 구현해야한다
    //커스텀 인터페이스를 커스텀 impl로 구현하고 이 인터페이스를 memberRepository 에 상속시키면 된다

    Page<MemberTeamDto> searchPageSimple(MemberSearchCond cond, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCond cond, Pageable pageable);
}
