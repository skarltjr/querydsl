package com.example.querydsl;

import com.example.querydsl.entity.Hello;
import com.example.querydsl.entity.QHello;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@Autowired
	EntityManager em;


	@Test
	void contextLoads() {
		Hello hello= new Hello();
		em.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello =new QHello("h");
		//  new ~ 할 필요없이 이미 qhello에 구현된게 있다
		//QHello hello1 = QHello.hello; 이거

		Hello hello1 = query
				.selectFrom(qHello)  // select h from Hello h 를 자바스럽게 짜도록 해주는게 query dsl
				.fetchOne();
		// 쿼리dsl을 쓸 때 이렇게  쿼리와관련된거는 덴티티말고 q엔티티를 넣는다.
		Assertions.assertThat(hello1).isEqualTo(hello);
	}

}
