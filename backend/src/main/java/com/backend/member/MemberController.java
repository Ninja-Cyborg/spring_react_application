package com.backend.member;

import com.backend.jwt.JWTUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;

    public MemberController(MemberService memberService, JWTUtil jwtUtil){
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<MemberDTO> getMembers(){
        return memberService.getAllMembers();
    }

    @GetMapping("{id}")
    public MemberDTO getMember(@PathVariable("id") Integer memberId){
        return memberService.getMember(memberId);
    }

    @PostMapping
    public ResponseEntity<?> registerMember(@RequestBody MemberRegistrationRequest registrationRequest){
        memberService.addMember(registrationRequest);
        // using email as it's unique
        String jwtToken = jwtUtil.issueToken(registrationRequest.email(), "ROLE_USER");

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .build();
    }

    @DeleteMapping("{id}")
    public void deleteMember(@PathVariable("id") Integer id){
        memberService.deleteMemberById(id);
    }

    @PutMapping("{id}")
    public void updateMember(@PathVariable("id") Integer id,
                             @RequestBody MemberUpdateRequest request){

        memberService.updateMember(id, request);
    }
}
