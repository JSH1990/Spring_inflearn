package com.studyolle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // 이 클래스가 Spring의 설정 클래스임을 정의
public class AppConfig { // 웹 보안 활성화, 웹 보안 설정 재정의

    /** passwordEncoder
     목적 : 비밀번호 언코딩
     설명 :
     비고 :
     **/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); //
    }
}
