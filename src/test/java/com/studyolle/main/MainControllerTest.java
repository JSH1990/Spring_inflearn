package com.studyolle.main;

import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.account.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest //@Extends나 @Runwith 작성안해도된다.
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm(); //유효성 검사 클래스
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm); //가입 확인 이메일 전송
    }

    @AfterEach
    void afterEach() throws Exception {
        accountRepository.deleteAll();
    }

    @DisplayName("이메일 로그인 성공")
    @Test
    void login_with_email() throws Exception {
        mockMvc.perform(post("/login") ///로그인 페이지로 이동.
                        .param("username", "test@email.com")
                        .with(csrf())
                        .param("password", "12345678")) //스프링 시큐리티가 알아서 login 처리해준다.
                .andExpect(status().is3xxRedirection()) // HTTP 응답 상태 코드가 3xx(리디렉션)인지 확인
                .andExpect(redirectedUrl("/")) //URL(/)로 리디렉션되는지를 확인
                .andExpect(authenticated().withUsername("test")); //이 부분은 사용자가 성공적으로 인증되었는지 확인
        // UserAccount클래스에서 account.getNickname으로 호출했기때문에 이메일 사용하지않는것이다.
    }

    @DisplayName("닉네임 로그인 성공")
    @Test
    void login_with_nickname() throws Exception {

        mockMvc.perform(post("/login") ///로그인 페이지로 이동.
                        .param("username", "test")
                        .with(csrf())
                        .param("password", "12345678")) //스프링 시큐리티가 알아서 login 처리해준다.
                .andExpect(status().is3xxRedirection()) // HTTP 응답 상태 코드가 3xx(리디렉션)인지 확인
                .andExpect(redirectedUrl("/")) //URL(/)로 리디렉션되는지를 확인
                .andExpect(authenticated().withUsername("test")); //이 부분은 사용자가 성공적으로 인증되었는지 확인
        // UserAccount클래스에서 account.getNickname으로 호출했기때문에 이메일 사용하지않는것이다.
    }

    @DisplayName("로그인 실패")
    @Test
    void login_fail() throws Exception {

        mockMvc.perform(post("/login") ///로그인 페이지로 이동.
                        .param("username", "test1")
                        .param("password", "123456781")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}