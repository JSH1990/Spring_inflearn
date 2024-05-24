package com.studyolle.account;

import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.account.form.SignUpForm;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    //컨트롤러의 코드를 서비스에 넣고, 다시 한번 테스트 돌려 잘되는지 확인해야한다.

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

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

//        Account account = Account.builder()
//                .email(signUpForm.getEmail())
//                .nickname(signUpForm.getNickname())
//                .password(passwordEncoder.encode(signUpForm.getPassword())) //raw비밀번호 말고 , raw비밀번호 암호화 + salt 적용.
//                .studyCreatedByWeb(true)
//                .studyEnrollmentResultByWeb(true)
//                .studyUpdatedByWeb(true)
//                .build();
        account.generateEmailCheckToken();
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
     설명 :
     비고 :
     **/
    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디올래, 로그인 링크");
        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        javaMailSender.send(mailMessage);
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
}
