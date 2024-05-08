package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.TokenProvider;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.exception.NotFoundException;
import roomescape.service.dto.request.LoginRequest;
import roomescape.service.dto.response.MemberResponse;

@Service
public class MemberService {

    private final MemberDao memberDao;
    private final TokenProvider tokenProvider;

    public MemberService(MemberDao memberDao, TokenProvider tokenProvider) {
        this.memberDao = memberDao;
        this.tokenProvider = tokenProvider;
    }

    public List<MemberResponse> findAll() {
        return memberDao.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    public String login(LoginRequest loginRequest) {
        Member member = memberDao.findByEmailAndPassword(
                        loginRequest.email(),
                        loginRequest.password())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        return tokenProvider.createToken(member);
    }

    public MemberResponse findMember(String token) {
        String payload = tokenProvider.getPayload(token);
        Member member = memberDao.findById(Long.valueOf(payload))
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        return MemberResponse.from(member);
    }

    public Member findLoginMember(String token) {
        String payload = tokenProvider.getPayload(token);
        Member member = memberDao.findById(Long.valueOf(payload))
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        return member;
    }
}
