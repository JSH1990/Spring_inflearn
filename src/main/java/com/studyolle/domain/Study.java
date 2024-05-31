package com.studyolle.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    /*
    Q. 변수 초기화시 new HashSet<>(); 하는이유?
    A. 필드가 null이 되지 않도록 방지하고, 즉시 사용할 수 있도록 하기 위해서이다.
        managers 필드는 초기값이 null이므로, 이 필드에 접근하려고 할 때 NullPointerException이 발생할 수 있다.
     */
    @ManyToMany
    private Set<Account> managers = new HashSet<>(); //스터디를 만든 사람

    @ManyToMany
    private Set<Account> members = new HashSet<>(); //

    @Column(unique = true)
    private String path; //url경로 이므로 unique.

    private String title; //스터디이름

    private String shortDescription; //스터디제목아래에 짧게 한줄보여줌

    @Lob @Basic(fetch = FetchType.EAGER) //길이 많기때문에 @Lob
    private String fullDescription; //본문

    @Lob @Basic(fetch = FetchType.EAGER) //길이 많기때문에 @Lob
    private String image; //프로필이미지

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime; //스터디 공개시간

    private LocalDateTime closedDateTime; //스터디 종료시간

    private LocalDateTime recruitingUpdatedDateTime; //인원모집 시간

    private boolean recruiting; //인원모집중인지 여부
    
    private boolean published; //공개 유무

    private boolean closed; //종료 유무

    private boolean useBanner; //배너 사용유무

    public void addManager(Account account) {
        this.managers.add(account);
    }

}
