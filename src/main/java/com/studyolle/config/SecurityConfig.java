package com.studyolle.config;

import com.studyolle.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

/** SecurityConfig 스프링 시큐리티 설정 **/
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final DataSource dataSource;

    /** configure
     목적 : 페이지 권한 허용
     설명 : 시큐리티 권한 페이지 허용 설정
     비고 :
     **/
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/sign-up", "/check-email", "/check-email-token",
                        "/email-login", "/check-email-login", "/login-link").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated();

        http.formLogin() //http.formLogin()만 있으면 스프링 시큐리티가 제공하는 로그인폼이 나온다.
                .loginPage("/login").permitAll(); //커스텀할수있는 로그인폼 페이지 설정

        http.logout()
                .logoutSuccessUrl("/"); //로그아웃 성공할때 이동하는 페이지 설정

        http.rememberMe()
                .userDetailsService(accountService) //
                .tokenRepository(tokenRepository()); //Username, 토큰 , 시리즈 세개를 조합해 만든 번호를 db에 저장
    }

    /** tokenRepository
     목적 :  remember-Me기능
     설명 :  spring Security의 Remember-Me 기능을 사용하기 위해 필요한 토큰 저장소를 설정하는 메서드
     비고 :
     **/
    @Bean //@Bean어노테이션은 Spring 컨테이너에 의해 관리되는 Bean 객체를 정의. 이 메서드가 반환하는 객체는 Spring 컨텍스트에서 Bean으로 등록되고 관리
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl(); //JdbcTokenRepositoryImpl 클래스를 사용하여 Remember-Me 토큰을 데이터베이스에 저장하고 관리
        jdbcTokenRepository.setDataSource(dataSource); //인스턴스에 데이터베이스 연결을 설정
        return jdbcTokenRepository;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()); //img 시큐리티 허용 코드
    }
}
