package com.studyolle.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** CurrentUser
 목적 : @CurrentUser 어노테이션은 현재 인증된 사용자 정보를 컨트롤러 메서드 파라미터에 쉽게 주입하기 위해 사용
 설명 : 커스텀 어노테이션은 컨트롤러 메서드의 파라미터에서 현재 인증된 사용자 정보를 쉽게 주입받기 위해 사용
 비고 : 익명 사용자인 경우 null을 반환하고, 인증된 사용자인 경우 사용자 정보를 반환하도록 설정
 **/
@Retention(RetentionPolicy.RUNTIME) //실행 중에도 리플렉션을 통해 이 어노테이션을 조회가능
@Target(ElementType.PARAMETER)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account") //사용자가 인증되지 않은 상태(익명 사용자)로 /profile URL에 접근하면, @CurrentUser 어노테이션의 SpEL 표현식에 따라 account 파라미터는 null
public @interface CurrentUser {
}
