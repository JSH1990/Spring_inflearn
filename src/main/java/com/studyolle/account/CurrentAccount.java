package com.studyolle.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** CurrentAccount 현재 로그인한 사용자 **/
@Retention(RetentionPolicy.RUNTIME)//실행 중인 동안 유지되어야 함
@Target(ElementType.PARAMETER) //@CurrentAccount를 사용하기 위해서
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account") //#this가 'anonymousUser'와 같으면 null을 반환하고, 그렇지 않으면 account를 반환.
public @interface CurrentAccount {
}
