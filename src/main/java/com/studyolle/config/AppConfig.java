package com.studyolle.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.spi.NameTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/** AppConfig 비밀번호 언코딩 **/
@Configuration // 이 클래스가 Spring의 설정 클래스임을 정의
public class AppConfig { // 웹 보안 활성화, 웹 보안 설정 재정의

    /** passwordEncoder
     목적 : @Bean 애노테이션을 사용하여 passwordEncoder 객체를 빈으로 등록
     설명 : 비밀번호 언코딩
     비고 :
     **/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); //
    }

    /** modelMapper
     목적 : @Bean 애노테이션을 사용하여 ModelMapper 객체를 빈으로 등록
     설명 :
     비고 :
     **/
    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE)
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
        return modelMapper;
    }
}
