package com.studyolle.settings.validator;

import com.studyolle.settings.form.PasswordForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** PasswordFormValidator 비밀번호 유효성 검사 **/
public class PasswordFormValidator implements Validator {


    /** supports
     목적 : 비밀번호 유효성 검사 할 클래스 선택
     설명 :  Validator가 특정 클래스의 인스턴스를 지원하는지 여부를 확인
     비고 :
     **/
    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz); //PasswordForm 또는 그 하위 클래스인 경우 true를 반환
    }

    /** validate
     목적 : passwordForm 객체 검증
     설명 : PasswordForm 객체의 새 비밀번호와 비밀번호 확인 필드가 일치하는지 검증
     비고 :
     **/
    @Override
    public void validate(Object target, Errors errors) {
      PasswordForm passwordForm = (PasswordForm)target; //target 객체를 PasswordForm 타입으로 캐스팅
      if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){ //passwordForm.getNewPassword()와 passwordForm.getNewPasswordConfirm()가 일치하는지 확인
          errors.rejectValue("newPassword", "wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
          //첫 번째 인자 "newPassword"는 오류가 발생한 필드의 이름
          //두 번째 인자 "wrong.value"는 오류 코드
          //세 번째 인자 사용자에게 표시될 기본 오류 메시지
      }
    }
}
