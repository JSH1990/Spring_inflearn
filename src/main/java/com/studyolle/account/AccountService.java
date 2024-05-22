package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;

/*
@Transactional을 붙여야하는 이유
processNewAccount메서드는 가입이 된 회원의 이메일 토큰을 생성해 가입확인 이메일을 전송하는 메서드이다.
하지만 Account newAccount = saveNewAccount(signUpForm);에서 saveNewAccount(signUpForm)여기는 이미 DB에 저장이 된 상태라서,
이 상태에서 새롭게 다시 나왔기 때문에, 영속성은 사라지게 된 detached 상황이다.
그래서 @Transactional붙여 즉시 반영할수있게 애너테이션을 붙인것이다.
 */

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    //컨트롤러의 코드를 서비스에 넣고, 다시 한번 테스트 돌려 잘되는지 확인해야한다.

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    /** processNewAccount
     목적 : 가입 확인 이메일 전송
     설명 : 회원가입시 작성했던 이메일주소로 다시 한번 확인을 하기 위한 목적.
     비고 : 이메일 토큰 생성
     **/
    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm); //signUpForm(등록한회원정보)를 가지고 saveNewAccount에 넣어줌
        newAccount.generateEmailCheckToken(); //토큰생성
        sendSignUpConfirmEmail(newAccount); //가입확인 이메일 전송
        return newAccount;
    }

    /** saveNewAccount
     목적 : 신규회원 저장
     설명 : 신규회원 정보 저장
     비고 : 비밀번호 암호화
     **/
    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword())) //raw비밀번호 말고 , raw비밀번호 암호화 + salt 적용.
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
        return accountRepository.save(account);
    }

    /** sendSignUpConfirmEmail
     목적 : 메일 인증 커스텀
     설명 :
     비고 :
     **/
    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("스터디올래, 회원 가입 인증"); //메일 제목
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail()); //메일 본문
        javaMailSender.send(mailMessage); //메일 전송
    }

    /** loadUserByUsername
     목적 : 회원유무검색
     설명 : 회원저장소에서 id,닉네임가 null이면 예외처리하고, 있으면 UserAccount반환
     비고 : UserDetailsService implements 할때 오버라이드 되는 메서드
     **/
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null){
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null){
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    /** login
     목적 : 로그인
     설명 : account객체, 비밀번호, 권한
     비고 :
     **/
    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))); //계정이 가지고있는 권한
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
