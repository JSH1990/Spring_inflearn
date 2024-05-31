package com.studyolle.study;

import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository repository;

    /** createNewStudy
     목적 : 스터디 개설
     설명 :
     **/
    public Study createNewStudy(Study study, Account account) {
        Study newStudy = repository.save(study); //스터디를 repository에 저장 후, newStudy에 넣는다.
        newStudy.addManager(account); //newStudy에 매니저 정보를 넣어서 반환한다.
        return newStudy;
    }
}
