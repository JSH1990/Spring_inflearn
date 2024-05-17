package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    //컨트롤러의 코드를 서비스에 넣고, 다시 한번 테스트 돌려 잘되는지 확인해야한다.

    private final AccountRepository accountRepository; //계정 보관소
    private final JavaMailSender javaMailSender; //메일
    private final PasswordEncoder passwordEncoder;

    /*
    @Transactional을 붙여야하는 이유
    processNewAccount메서드는 가입이 된 회원의 이메일 토큰을 생성해 가입확인 이메일을 전송하는 메서드이다.
    하지만 Account newAccount = saveNewAccount(signUpForm);에서 saveNewAccount(signUpForm)여기는 이미 DB에 저장이 된 상태라서,
    이 상태에서 새롭게 다시 나왔기 때문에, 영속성은 사라지게 된 detached 상황이다.
    그래서 @Transactional붙여 즉시 반영할수있게 애너테이션을 붙인것이다.
     */
    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm); //signUpForm(등록한회원정보)를 가지고 saveNewAccount에 넣어줌
        newAccount.generateEmailCheckToken(); //토큰생성
        sendSignUpConfirm(newAccount); //가입확인 이메일 전송
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword())) //raw비밀번호 말고 , raw비밀번호 암호화 + salt 적용.
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account); // 회원가입 정보 넣어서 계정 보관소에 저장
        return newAccount;
    }

    private void sendSignUpConfirm(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("스터디올래, 회원 가입 인증"); //메일 제목
        mailMessage.setText("/check-email-token?token="+ newAccount.getEmailCheckToken()+
                "&email=" + newAccount.getEmail()); //메일 본문

        javaMailSender.send(mailMessage); //메일 전송
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                account.getNickname(), //닉네임
                account.getPassword(), //비밀번호
                List.of(new SimpleGrantedAuthority("ROLE_USER")));//계정이 가지고있는 권한
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }
}
