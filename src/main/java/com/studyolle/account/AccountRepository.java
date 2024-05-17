package com.studyolle.account;

import com.studyolle.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true) //readOnly = true로 성능의 이점을 가져온다.
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);
}
