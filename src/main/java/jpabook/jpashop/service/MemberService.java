package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// JPA의 모든 데이터 변경이나 로직들은 가급적이면 트랜잭션 안에서 실행되어야함.
// @Transactional 필요. => LazyLoading 이런게 가능
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor // 필드를 가지고 생성자 만들어주는 것.
public class MemberService {

    private final MemberRepository memberRepository;

    /* @RequiredArgsConstructor가 하는 일
    필드 주입 방식:
    @Autowired
    MemberRepository memberRepository;

    (권장) 생성자 주입 방식:
    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    */

    // 회원 가입. 이건 readOnly = true 하면 안 됨.
    @Transactional
    public Long join(Member member) {
        // 중복 회원 검증
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 단건 조회
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    /**
     * 회원 수정
     */
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
