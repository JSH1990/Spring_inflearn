package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;


/**
 * Class Name : AccountController
 * Description : 로그인
 */
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String sighUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm()); //model.addAttribute(new SignUpForm()); 이렇게 사용해도 된다.
        return "account/sign-up";
    }

    /*
    로그인 입력
     */
    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){ //검증 - SignUpForm에 적용한 errors에 걸리면 회원가입창으로 다시 넘어간다.
        if(errors.hasErrors()) {
            return "account/sign-up";
        }

        //기존에 컨트롤러에 있던 코드를 processNewAccount메서드를 생성해,
        //accountService뒤로 숨김(리펙토링)
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        //회원 가입처리
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view =  "account/checked-email";
        if(account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return view;
        }

        account.completeSignUp(); //이메일의 토큰이 맞는지 확인하면 자동으로 로그인
        accountService.login(account);
        model.addAttribute("numberOfUser", accountRepository.count()); //전체 회원 수
        model.addAttribute("nickname", account.getNickname()); //회원가입한 닉네임
        return view;
    }

}
