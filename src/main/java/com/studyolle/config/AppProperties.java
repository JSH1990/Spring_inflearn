package com.studyolle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app")
public class AppProperties { //AppProperties 클래스는 외부 설정 파일(application.properties)에서 app 접두사를 가진 프로퍼티들을 읽어와 바인딩하는 역할

    private String host;

}
