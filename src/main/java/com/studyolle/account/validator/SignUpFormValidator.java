package com.studyolle.account.validator;

import com.studyolle.account.AccountRepository;
import com.studyolle.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** SignUpFormValidator 회원가입 유효성 검사 **/
@Component
@RequiredArgsConstructor
/*
    Q. @RequiredArgsConstructor?

    A. 롬복(Lombok) 라이브러리에서 제공하는 애노테이션 중 하나로, 클래스의 모든 final 필드와 @NonNull이 붙은 필드들을 인자로 받는 생성자를 자동으로 생성
 */
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    /** supports
     목적 : 회원가입 유효성 검사 할 클래스 선택
     설명 :  Validator가 특정 클래스의 인스턴스를 지원하는지 여부를 확인
     비고 :
     **/
    @Override
    public boolean supports(Class<?> aClazz) {
        return aClazz.isAssignableFrom(SignUpForm.class);
    }

    /** validate
     목적 : 중복 확인.
     설명 : 이메일과 닉네임이 중복되는지 확인
     비고 :
     **/
    //이메일과 닉네임이 중복되는지 확인하는 메서드.
    @Override
    public void validate(Object object, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) object;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getEmail()},"이미 사용중인 닉네임입니다.");
        }
    }

}
