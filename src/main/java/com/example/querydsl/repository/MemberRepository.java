package com.example.querydsl.repository;

import com.example.querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**     인터페이스는 여러개를 상속받을 수 있다 */

public interface MemberRepository extends JpaRepository<Member,Long> ,MemberRepositoryCustom{
    List<Member> findByUsername(String username);

}
