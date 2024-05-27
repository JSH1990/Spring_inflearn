package com.studyolle.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/** SignUpFormValidator 회원가입 Form 유효성 검사 **/
@Data
public class SignUpForm {
    
    /*
    스프링에서 빈으로 등록되는 애노테이션
    @Component / @Repository / @Service / @Controller

    @RestController - @Controller와 @ResponseBody가 결합된 형태로, RESTful 웹 서비스의 컨트롤러

    @Configuration - 주로 빈 정의와 관련된 설정을 포함하며, @Bean 애노테이션을 사용하여 메서드 수준에서 빈을 정의

    @Bean - @Configuration 클래스 내에서 메서드에 사용되어 메서드의 반환 객체를 스프링 빈으로 등록
     */

    @NotBlank
    @Length(min = 3, max = 20)
    @Pattern(regexp =  "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$")
    private String nickname;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 8, max = 50)
    private String password;
}
