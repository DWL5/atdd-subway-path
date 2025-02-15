package wooteco.subway.auth.application;

import org.springframework.stereotype.Service;
import wooteco.subway.auth.dto.TokenRequest;
import wooteco.subway.auth.dto.TokenResponse;
import wooteco.subway.auth.infrastructure.JwtTokenProvider;
import wooteco.subway.member.dao.MemberDao;
import wooteco.subway.member.domain.Member;

@Service
public class AuthService {

    private final MemberDao memberDao;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberDao memberDao, JwtTokenProvider jwtTokenProvider) {
        this.memberDao = memberDao;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse createToken(TokenRequest tokenRequest) {
        Long id = getIdWhenValidLogin(tokenRequest);
        String accessToken = jwtTokenProvider.createToken(id);
        return new TokenResponse(accessToken);
    }

    public long getIdWhenValidLogin(TokenRequest tokenRequest) {
        return memberDao.findByEmail(tokenRequest.getEmail())
                .filter(member ->
                        member.haveSameInfo(tokenRequest.getEmail(), tokenRequest.getPassword()))
                .map(Member::getId)
                .orElseThrow(AuthorizationException::new);
    }

    public Member findMemberByToken(String token) {
        jwtTokenProvider.validateToken(token);
        Long id = jwtTokenProvider.getIdFromPayLoad(token);
        return memberDao.findById(id);
    }
}
