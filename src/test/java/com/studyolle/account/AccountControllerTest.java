package com.studyolle.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc //Spring Boot에서 제공하는 테스트 어노테이션이다. MockMvc는 실제 서버를 띄우지 않고도 애플리케이션의 HTTP 엔드포인트를 테스트
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;

    @DisplayName("회원가입 화면 보이는지 테스트")
    @Test
    void signUp() throws Exception {
        mockMvc.perform(get("/sign-up")) //mockMvc객체로 "/sign-up"경로로 GET요청.
                .andDo(print()) //뷰의 바디 확인할수 있음
                .andExpect(status().isOk()) //응답의 HTTP 상태 코드가 200 OK인지 확인. 아니면 테스트 실패.
                .andExpect(view().name("account/sign-up")) //반환한 뷰의 이름이 "account/sign-up" 인지 확인.
                .andExpect(model().attributeExists("signUpForm")); //model객체 attribute에 "signUpForm" 이라는 이름이 존재하는지 확인.
    }
}