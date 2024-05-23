package com.studyolle.settings.validator;

import com.studyolle.account.AccountRepository;
import com.studyolle.domain.Account;
import com.studyolle.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** NicknameValidator 닉네임 유효성 검사 **/
@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    /** supports
     목적 : 닉네임 유효성 검사 할 클래스 선택
     설명 :  Validator가 특정 클래스의 인스턴스를 지원하는지 여부를 확인
     비고 :
     **/
    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    /** validate
     목적 : nicknameForm 객체 검증
     설명 : nicknameForm 객체의 새 닉네임과 닉네임 확인 필드가 일치하는지 검증
     비고 :
     **/
    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;
        Account byNickname = accountRepository.findByNickname(nicknameForm.getNickname());
        if (byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다.");
        }
    }
}
