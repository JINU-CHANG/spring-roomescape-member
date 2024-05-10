package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.LoginMember;
import roomescape.domain.Member;
import roomescape.exception.BadRequestException;
import roomescape.service.MemberService;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_NAME = "token";

    private final MemberService memberService;

    public LoginMemberArgumentResolver(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        if (request.getCookies() == null) {
            throw new BadRequestException("쿠키가 존재하지 않습니다."); //TODO 로그인페이지에서 쿠키 존재하지 않는다는 에러 나오지 않게
        }

        String token = extractTokenFromCookie(request.getCookies());

        Member member = memberService.findLoginMember(token);
        return new LoginMember(member.getId(), member.getName());
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TOKEN_NAME)) {
                return cookie.getValue();
            }
        }

        throw new BadRequestException("올바르지 않은 토큰값입니다.");
    }
}
