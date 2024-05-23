package com.studyolle;

import com.studyolle.account.AccountService;
import com.studyolle.account.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/** Spring Security를 사용하여 테스트 환경에서 특정 사용자를 인증하는 기능을 제공 **/
@RequiredArgsConstructor //필드를 초기화하기 위한 생성자를 자동으로 생성
public class WithAccountSecurityContextFacotry implements WithSecurityContextFactory<WithAccount> { //커스텀 보안 컨텍스트를 생성

    private final AccountService accountService;

    /** createSecurityContext
     목적 : 사용자 생성
     설명 : 스프링 시큐리티를 이용해 사용자 인증
     비고 : @WithAccount 애노테이션을 이용해, 테스트에서 시큐리티 인증 유저로 만들어준다.
     **/
    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String nickname = withAccount.value();

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail(nickname + "@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm); //새로운 계정을 생성

        UserDetails principal = accountService.loadUserByUsername(nickname); //사용자의 세부 정보를 principal변수에 담음
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities()); //인증객체생성
        SecurityContext context = SecurityContextHolder.createEmptyContext(); //새로운 SecurityContext를 생성하고, 인증 객체를 설정
        context.setAuthentication(authentication);
        return context;
    }
}
