package com.studyolle;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Spring Security를 사용하여 테스트 환경에서 특정 사용자를 인증하는 기능을 제공 **/
@Retention(RetentionPolicy.RUNTIME) //런타임 동안 유지
@WithSecurityContext(factory = WithAccountSecurityContextFacotry.class) //Spring Security의 테스트 컨텍스트에 보안 설정을 추가하는 데 사용
public @interface WithAccount {

    String value();

}
