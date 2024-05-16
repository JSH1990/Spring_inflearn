package com.studyolle.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class AccountController {

    @GetMapping("/sign-up")
    public String sighUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm()); //model.addAttribute(new SignUpForm()); 이렇게 사용해도 된다.
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){ //SignUpForm에 적용한 errors에 걸리면 회원가입창으로 다시 넘어간다.
        if(errors.hasErrors()) {
            return "account/sign-up";
        }

        //회원 가입처리
        return "redirect:/";
    }
}
