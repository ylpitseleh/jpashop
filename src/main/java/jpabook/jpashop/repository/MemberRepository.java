package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    // @PersistenceContext : 스프링이 엔티티 매니저를 만들어서 주입해줌.
    // JPA에서는 @PersistenceContext를 @Autowired로 바꿀 수 있고,
    // @Autowired를 @RequiredArgsConstructor로 변경.
    private final EntityManager em;

    // 저장
    public void save(Member member) {
        em.persist(member);
    }

    // 조회
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    // 리스트 조회
    public List<Member> findAll() {
        // JPQL을 작성해야 함.
        // SQL = 테이블을 대상으로 쿼리 / JPQL = 엔티티 객체를 대상으로 쿼리.
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // 특정 회원 이름 검색
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name",
                Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
