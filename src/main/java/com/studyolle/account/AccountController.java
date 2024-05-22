package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

/** AccountController 사용자 컨트롤러 **/
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


        /*
        Q. 아래 코드에서는 사용자프로필의 가입날짜가 표기되지않는다.
        A. 트랜잭션 범위 밖에서 일어난 일이기 때문이다.
           위의 accountRepository.findByEmail(email)에서의 내용까지만 트랜잭션되었고,
           이후의 accountService.completeSignUp(account); 여기는 트랜잭션되지않았기때문에 Service레이어안으로 넣고, @Transationl붙인다.

           1. 데이터 변경은 서비스 계층으로 위임해서 트랜잭션안에서 처리한다.
           2. 데이터 조회는 리파지토리 또는 서비스를 사용한다.
         */
//        accountService.completeSignUp(account);
//        account.completeSignUp(); //이메일의 토큰이 맞는지 확인하면 자동으로 로그인
//        accountService.login(account);

        accountService.completeSignUp(account);
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    /** checkEmail
     목적 : 이메일 체크 화면
     설명 : 회원가입 후, "스터디올레 가입을 완료하려면 계정 인증 이메일을 확인하세요." 클릭하면 나오는 화면
     비고 : 현재 입력한 email를 담아 뷰로 보낸다.
     **/
    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail()); //회원가입시 입력했던 정보들을 model 객체에 담는다.
        return "account/check-email";
    }

    /** resendConfirmEmail
     목적 : 이메일 체크 재전송
     설명 : 인증 이메일이 1시간 이내이면 재전송 불가
     비고 : 
     **/
    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) { //보낸 이메일이 1시간이 경과되지 않다면, error 메세지
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    /** viewProfile
     목적 : 프로필 관리
     설명 : 닉네임 조회를 통해, 프로필 사용자가 맞는지 체크
     비고 : 
     **/
    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if(nickname == null){
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다."); //예외를 던짐
        }

        model.addAttribute(byNickname); //model.addAttribute("account",byNickname);
        model.addAttribute("isOwner", byNickname.equals(account)); //계정의 주인이 맞는지 확인.
        return "account/profile";
    }

}
