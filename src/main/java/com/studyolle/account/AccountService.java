package com.studyolle.account;

import com.studyolle.config.AppProperties;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.account.form.SignUpForm;
import com.studyolle.domain.Zone;
import com.studyolle.mail.EmailMessage;
import com.studyolle.mail.EmailService;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/*
@Transactional을 붙여야하는 이유
processNewAccount메서드는 가입이 된 회원의 이메일 토큰을 생성해 가입확인 이메일을 전송하는 메서드이다.
하지만 Account newAccount = saveNewAccount(signUpForm);에서 saveNewAccount(signUpForm)여기는 이미 DB에 저장이 된 상태라서,
이 상태에서 새롭게 다시 나왔기 때문에, 영속성은 사라지게 된 detached 상황이다.
그래서 @Transactional붙여 즉시 반영할수있게 애너테이션을 붙인것이다.
 */

/** AccountService 사용자 서비스 **/
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    //컨트롤러의 코드를 서비스에 넣고, 다시 한번 테스트 돌려 잘되는지 확인해야한다.

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine; //템플릿 엔진의 핵심 구성 요소로, 템플릿 파일을 처리하고 렌더링하는 역할을 한다.
    private final AppProperties appProperties; //context.setVariable("host", appProperties.getHost()); 사용하기 위해서

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
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    /** sendSignUpConfirmEmail
     목적 : 인증 메일 커스텀
     설명 : 인증 메일 형식을 타임리프로 뷰 페이지를 통해 html로 구현
     비고 : TemplateEngine, AppProperties 클래스 사용
     **/
    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context(); //Map 클래스와 같다고 생각하면 된다. (templateEngine)
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost()); //app.host=http://localhost:8080 (application.properties파일에 정의)
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    /** loadUserByUsername
     목적 : 회원유무검색
     설명 : 회원저장소에서 id,닉네임가 null이면 예외처리하고, 있으면 UserAccount반환
     비고 : UserDetailsService implements 할때 오버라이드 되는 메서드
     **/
    @Transactional(readOnly = true) //성능에 유리하므로 사용
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

    /** completeSignUp
     목적 : 이메일 인증완료
     설명 : 이메일 인증 후 로그인
     비고 :
     **/
    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    /** updateProfile
     목적 : 프로필 수정
     설명 : 프로필 수정 후 accountRepository.save(account)로 영속성 context로 관리
     비고 :
     **/
    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account); //profile의 값들이 account로 들어간다.
//        account.setUrl(profile.getUrl());
//        account.setOccupation(profile.getOccupation());
//        account.setLocation(profile.getLocation());
//        account.setBio(profile.getBio());
//        account.setProfileImage(profile.getProfileImage());

        /*
        이슈
        Q. accountRepository.save(account); 작성하지않으면, 프로필 설정은 update 되지않는다.

        A. 위의 completeSignUp(Account account) 와 updateProfile(Account account)의 account 객체는 서로 다른 객체이다.
           위의 account객체는 이미 영속성 context에 들어가 있는 객체이고,
           아래 account객체는 세션에 담겨있던 account 객체이다.
           그래서 detached상태의 객체이며(detached 상태의 객체는 한때 영속성 context에 의해 관리되었지만 더이상 관리되지않는 상태)
           다시 merge해서 영속성 컨텍스트에 병합시키기위해선, JPA에서 repository.save 메소드를 호출하면, 내부적으로 merge가 수행되어, 'detached' 상태의 객체가 영속성 컨텍스트에 병합된다.
         */
        accountRepository.save(account);
    }

    /** updatePassword
     목적 : 비밀번호 수정
     설명 : 새 비밀번호 encode 후에 저장
     비고 :
     **/
    public void updatePassword(Account account, String newPassword){
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account); //merge해서 영속성 컨텍스트에 병합
    }

    /** updateNotifications
     목적 : 알람 수정
     설명 : 알람 업데이트
     비고 :
     **/
    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
//        account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
//        account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
//        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
//        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
//        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
//        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());
        accountRepository.save(account);
    }

    /** updateNickname
     목적 : 닉네임 수정
     설명 : 닉네임 업데이트
     비고 :
     **/
    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account); //로그인을 다시 해줌 -- 다시해줘야 우측 계정 클릭할때 바뀐 닉네임으로 볼수있다.
    }

    /** sendLoginLink
     목적 : 패스워드 분실 메일 링크
     설명 : 인증 메일 형식을 타임리프로 뷰 페이지를 통해 html로 구현
     비고 : TemplateEngine, AppProperties 클래스 사용
     **/
    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    /** addTag
     목적 : 태그 추가
     설명 :
     **/
    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag)); //Optional 객체에 값이 존재하는지 확인. 값이 존재하면 true, 존재하지 않으면 false를 반환.
        //ifPresent(a -> a.getTags().add(tag)): 값이 존재하는 경우에만 a.getTags().add(tag)을 수행.

        /*
        Q. Optional이란?
        A. Optional<T>는 null이 올 수 있는 값을 감싸는 Wrapper 클래스로, 참조하더라도 NPE(null point excption)가 발생하지 않도록 도와준다
           Optional은 메소드의 결과가 null이 될 수 있으며, null에 의해 오류가 발생할 가능성이 매우 높을 때 반환값으로만 사용되어야 한다

        출처: https://mangkyu.tistory.com/70 [MangKyu's Diary:티스토리]
         */
    }

    /** getTags
     목적 : 태그 조회
     설명 :
     **/
    public Set<Tag> getTags(Account account){
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    /** removeTag
     목적 : 태그 제거
     설명 :
     **/
    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    /** getZones
     목적 : 지역 조회
     설명 :
     **/
    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    /** addZone
     목적 : 지역 추가
     설명 :
     **/
    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    /** removeZone
     목적 : 지역 제거
     설명 :
     **/
    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }
}
