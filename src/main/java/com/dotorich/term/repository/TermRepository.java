package com.dotorich.term.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.term.domain.Term;

public interface TermRepository extends JpaRepository<Term, Long> {

	/**
	 * 이름 부분 일치 검색. 정확히 같은 이름 → 앞부분 일치 → 나머지 순, 같은 순위는 가나다순.
	 * pattern 은 TermService 에서 %, _ 를 ! 로 이스케이프해서 넘긴다.
	 */
	@Query("""
		select t from Term t
		where t.name like concat('%', :pattern, '%') escape '!'
		order by
			case when t.name = :keyword then 0
				when t.name like concat(:pattern, '%') escape '!' then 1
				else 2 end,
			t.name
		""")
	List<Term> search(String keyword, String pattern, Pageable pageable);
}
