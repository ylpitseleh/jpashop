package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
/*
DTO 다른 클래스로 분리하지 않는 이유
= 해당 컨트롤러만 사용하면 이너클래스로 만들고,
해당 컨트롤러와 외에 다른 컨트롤러도 공용으로 사용하면 패키지와 클래스를 따로 만들거나 하는 고민을 한다.

이너 클래스는 이너 클래스를 포함하는 클래스 안에서만 한정적으로 접근할 때만 사용한다.
만약 여러 클래스에서 접근해야 하면 외부 클래스로 사용하는 것이 맞다.
이너 클래스의 이점은 해당 클래스 안에서만 한정적으로 사용한다는 의미를 부여할 수 있고, 덕분에 개발자 입장에서 신경써야 하는 외부 클래스들이 줄어드는 효과가 있다.
 */
// @RestController = @Controller + @ResponseBody
// ResponseBody = 데이터 자체를 바로 xml이나 json으로 보내자.
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /*
    !!! API를 만들 때는 항상 파라미터를 엔티티로 받지 마라 !!!
    파라미터에 @RequestBody @Valid Member member 넣으면 안 좋음. (엔티티 그대로 넣는 것)
    API 스펙을 위한 별도의 DTO를 만들어야 함.
    엔티티는 엄청 많은 곳에서 쓰이니까 @NotEmpty같은 걸로 변경시키면 X. @NotEmpty하면 안 되는 경우가 있을 수 있음.
    엔티티를 이렇게 외부에서 JSON 오는걸 바인딩 받아서 쓰면 안 됨. => 나중에 큰 장애 발생
     */
    /**
     * 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위
     한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * 결론
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     */
    // 사용 X -----------------------------------------------------------------------
    // @RequestBody = json으로 온 body를 member에 그대로 매핑해서 넣어줌.
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    // -------------------------------------------------------------------------------

    /**
     * 등록 V2: 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    /*
    <static인 이유>
    내부 클래스에 static을 선언하지 않으면 MemberController 클래스 외부에서 이 객체를 직접 생성할 수 없다.
    Response는 클래스 내부에서 생성해서 반환하기 때문에 static이 없어도 됨.
    반면에 Request는 클래스 외부에서 생성해서 들어오기 때문에 static이 없으면 객체를 생성할 수 없다.
     */
    // 나는 DTO라네
    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    /**
     * 수정 API
     */
    // 회원 수정도 DTO를 요청 파라미터에 매핑
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    /**
     * 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출한다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 기본적으로 엔티티의 모든 값이 노출된다.
     * - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
     * - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의
     API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * - 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다.(별도의 Result 클래스 생성
     으로 해결)
     * 결론
     * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     */
    //조회 V1: 안 좋은 버전, 모든 엔티티가 노출, @JsonIgnore -> 이건 정말 최악, api가 이거 하나인가! 화면에 종속적이지 마라!
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO를 반환한다.
     */
    // api 만들 때는 절대 엔티티를 노출하거나 받지 마라. 중간에 api 스펙에 맞는 dto를 만들어라 무조건!
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        //엔티티 -> DTO 변환
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect);
    }
    // 한 번 감싸줘야함. Collection이나 배열 타입으로 바로 return하면 json 배열타입으로 나가버려서 유연성이 떨어짐.
    @Data
    @AllArgsConstructor
    class Result<T> {
        private T data;
    }

    // 필요한 것만 스펙에 노출.
    @Data
    @AllArgsConstructor
    class MemberDto {
        private String name;
    }

}
