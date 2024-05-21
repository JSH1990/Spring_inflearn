package com.studyolle.account;

import com.studyolle.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/** AccountRepository
 목적 : 회원 저장소
 설명 : 이메일/닉네임를 저장하고있다.
 비고 :
 **/
@Transactional(readOnly = true) //readOnly = true로 성능의 이점을 가져온다.
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);
}
