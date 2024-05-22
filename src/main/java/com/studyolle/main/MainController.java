package com.studyolle.main;

import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** MainController 메인페이지 **/
@Controller
public class MainController {

    /** home
     목적 : 첫 화면
     설명 : account(계정)이 null이 아니면 model 객체에 넣고, index 페이지로 이동한다.
     비고 :
     **/
    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }

        return "index";
    }

    /** login
     목적 : 로그인화면
     설명 : "/login" url 요청이 오면 "login" 뷰페이지로 이동
     비고 :
     **/
    @GetMapping("/login")
    public String login(){
        return "login"; //"templates/login.html"; 생략됨
    }

}
