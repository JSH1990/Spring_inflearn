package com.studyolle.account;

import com.studyolle.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Class Name : AccountControllerTest
 * Description : 로그인 테스트
 */
@Transactional
@SpringBootTest
@AutoConfigureMockMvc //Spring Boot에서 제공하는 테스트 어노테이션이다. MockMvc는 실제 서버를 띄우지 않고도 애플리케이션의 HTTP 엔드포인트를 테스트
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountController accountController;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean //이메일 보냈는지 확인하기 위해서 MockBean 생성
    JavaMailSender javaMailSender;

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "qeqweqwe")
                .param("email","email@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated()); //인증이 된 사용자인지 아닌지 확인할수있다.
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken() throws Exception {
        Account account = Account.builder()
                .email("test@email.com")
                .password("12345678")
                .nickname("keesun")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email",newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("keesun")); //인증이 된 사용자인지 아닌지 확인할수있다.
    }

    @DisplayName("회원가입 화면 보이는지 테스트")
    @Test
    void signUp() throws Exception {
        mockMvc.perform(get("/sign-up")) //mockMvc객체로 "/sign-up"경로로 GET요청.
                .andDo(print()) //뷰의 바디 확인할수 있음
                .andExpect(status().isOk()) //응답의 HTTP 상태 코드가 200 OK인지 확인. 아니면 테스트 실패.
                .andExpect(view().name("account/sign-up")) //반환한 뷰의 이름이 "account/sign-up" 인지 확인.
                .andExpect(model().attributeExists("signUpForm")) //model객체 attribute에 "signUpForm" 이라는 이름이 존재하는지 확인.
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "sadasd")
                .param("email","email..")
                .param("password","12342")
                        .with(csrf())) //이 코드를 넣어주지않으면 테스트 실패한다. 그 이유는 SecurityConfig에서 접근을 허용해도 클라이언트에서 csrf토큰이 들어오지않으면, 데이터가 전송되지 않는다.
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "sadasd")
                        .param("email","email@email.com")
                        .param("password","12222342")
                        .with(csrf())) //이 코드를 넣어주지않으면 테스트 실패한다. 그 이유는 SecurityConfig에서 접근을 허용해도 클라이언트에서 csrf토큰이 들어오지않으면, 데이터가 전송되지 않는다.
                .andExpect(status().is3xxRedirection()) //HTTP 응답 상태 코드가 3xx 범위 내에 있는지(즉, 리다이렉션 상태인지) 확인한다.
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("sadasd"));

        Account account = accountRepository.findByEmail("email@email.com"); //이메일 중복확인
        assertNotNull(account); //이메일 중복 아니면
        assertNotEquals(account.getPassword(), "12222342"); //"암호화된 비밀번호" 와 "raw비밀번호" 가 동일한지 테스트
        assertNotNull(account.getEmailCheckToken()); //이메일전송 토큰 있는지 확인
        then(javaMailSender).should().send(any(SimpleMailMessage.class)); //아무 객체를 사용해 메일을 전송하고 확인한다.
    }



}