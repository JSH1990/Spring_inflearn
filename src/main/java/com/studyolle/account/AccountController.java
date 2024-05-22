package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    /** initBinder
     목적 : 유효성 검사
     설명 : 회원가입시 이메일 형식이 올바른지, 비밀번호가 특정 조건을 만족하는지 등을 검증.
     비고 : @InitBinder는 컨트롤러 클래스 내의 특정 메서드에 붙여서 사용.
     **/
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    /** signUpForm
     목적 : 회원가입 페이지
     설명 : 회원가입 페이지를 뷰로 보여준다.
     비고 :
     **/
    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    /** signUpSubmit
     목적 : 회원가입 등록
     설명 : 회원가입 할때 signUpForm 객체에 개인정보 담는다.
     비고 : 회원가입 -> 성공시 "/" 실패시 "/로그인화면"
     **/
    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) { //검증 - SignUpForm에 적용한 errors에 걸리면 회원가입창으로 다시 넘어간다.
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        //기존에 컨트롤러에 있던 코드를 processNewAccount메서드를 생성해,
        //accountService뒤로 숨김(리펙토링)
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        //회원 가입처리
        return "redirect:/";
    }

    /** checkEmailToken
     목적 : 이메일 토큰 확인 여부
     설명 : 임시가입된 회원의 이메일 정보나 메일로 온 토큰의 값이 일치하지 않으면 에러메세지를 보내고, 메일 토큰이 일치하면 자동 로그인한다.
     비고 : 반환값을 "account/checked-email" 뷰로 이동시킨다.
     **/
    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        account.completeSignUp(); //이메일의 토큰이 맞는지 확인하면 자동으로 로그인
        accountService.login(account);
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

}
