package com.studyolle.settings;

import com.studyolle.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Profile 사용자프로필 form을 채울 클래스 **/
@Data
@NoArgsConstructor
public class Profile {

        private String bio;

        private String url;

        private String occupation;

        private String location;

        public Profile(Account account) {
            this.bio = account.getBio();
            this.url = account.getUrl();
            this.occupation = account.getOccupation();
            this.location = account.getLocation();
        }
}
