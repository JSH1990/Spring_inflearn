package com.studyolle.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of="id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id; //primary key를 id로 설정

    @Column(unique = true) //email과 nickname으로 로그인할수 있도록 해야하기때문에 unique 설정.
    private String email;

    @Column(unique = true)
    private String nickname; //DB에서는 기본값으로 varchar(255)로 저장됨

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;
    
    private String liveAround;
    
    private String locationl;
    
    @Lob
    @Basic(fetch = FetchType.EAGER) //이미지는 문자열이 더 크기때문에 @Lob 설정
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

}
