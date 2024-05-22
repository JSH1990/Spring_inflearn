package com.studyolle.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/** PersistentLogins
 목적 : Remember-Me 기능을 지원하기 위해 Spring Security에서 사용되는 테이블의 구조를 정의
 설명 : 데이터베이스 테이블과 매핑되어 Remember-Me 토큰을 저장하는 데 사용
 비고 :
 **/
@Table(name = "persistent_logins") //이 엔티티가 persistent_logins라는 테이블에 매핑됨을 나타냄
@Entity
@Getter @Setter
public class PersistentLogins {

    @Id
    @Column(length = 64)
    private String series;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(name = "last_used", nullable = false, length = 64) //토큰이 마지막으로 사용된 시간을 저장
    private LocalDateTime lastUsed;

}
