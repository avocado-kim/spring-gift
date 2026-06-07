package gift.member.service;
import gift.member.domain.Member;
import gift.member.dto.MemberRequest;
import gift.member.repository.MemberRepository;

import gift.auth.jwt.JwtProvider;
import gift.auth.dto.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public MemberService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    public TokenResponse register(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        Member member = memberRepository.save(new Member(email, password));
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }

    public TokenResponse login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (member.getPassword() == null || !member.getPassword().equals(password)) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return new TokenResponse(jwtProvider.createToken(member.getEmail()));
    }
}