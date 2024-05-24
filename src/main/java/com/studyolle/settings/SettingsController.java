package com.studyolle.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.*;
import com.studyolle.settings.validator.NicknameValidator;
import com.studyolle.settings.validator.PasswordFormValidator;
import com.studyolle.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
/** SettingsController 프로필 설정 **/
public class SettingsController {

    /* 프로필 */
    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/" + SETTINGS_PROFILE_VIEW_NAME;
    /* 패스워드 */
    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
    static final String SETTINGS_PASSWORD_URL = "/" + SETTINGS_PASSWORD_VIEW_NAME;
    /* 알림 */
    static final String SETTINGS_NOTIFICATIONS_VIEW_NAME = "settings/notifications";
    static final String SETTINGS_NOTIFICATIONS_URL = "/" + SETTINGS_NOTIFICATIONS_VIEW_NAME;
    /* 계정 */
    static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account";
    static final String SETTINGS_ACCOUNT_URL = "/" + SETTINGS_ACCOUNT_VIEW_NAME;
    /* 태그 */
    static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags";
    static final String SETTINGS_TAGS_URL = "/" + SETTINGS_TAGS_VIEW_NAME;

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    /*
    Q. ObjectMapper?
    A. JSON 형식을 사용할 때, 응답들을 직렬화하고 요청들을 역직렬화 할 때 사용하는 기술이다.

    직렬화 - 데이터를 전송하거나 저장할때, 바이트 문자열이어야 하기때문에 객체들을 문자열로 바꾸어 주는것 (Object -> String 문자열)
   역직렬화 - 데이터가 모두 전송된 이후, 수신측에서 다시 문자열을 기존 객체로 회복시켜주는것 (String 문자열 -> Object)

   스프링 부트의 경우, spring-boot-starter-web에 기본적으로 Jackson 라이브러리가 있어서 Object <->JSON 간 변환은 자동으로 처리된다.
   (Jackson 라이브러리란 자바에서 고수준의 JSON 처리기이다.)
     */
    private final ObjectMapper objectMapper;

    /** initBinder
     목적 : 데이터 바인딩 및 검증
     설명 : passwordForm과 관련된 요청이 있을 때마다 PasswordFormValidator를 사용하여 검증을 한다.
     비고 : @initBinder이란? 특정 컨트롤러에서 입력 데이터의 변환, 검증 등을 설정하는 메서드에 적용
     **/
    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) { //PasswordFormValidator 인스턴스를 WebDataBinder에 추가하여, passwordForm 객체에 대한 검증을 수행하도록 설정
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    /** profileUpdateForm
     목적 : 프로필 설정 폼
     설명 : 현재 접속해 있는 @CurrentUser account객체와 프로필 설정 new Profile(account) 객체를 model에 담아 뷰로 보낸다.
     비고 :
     **/
    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    /** updateProfile
     목적 : 프로필 수정
     설명 : 프로필을 수정하고, "프로필을 수정했습니다." 메세지 표시
     비고 :
     **/
    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }

        accountService.updateProfile(account, profile); //profile클래스에 빈생성자 만들지않으면 null 값찍힌다.
        attributes.addFlashAttribute("message", "프로필을 수정했습니다."); //addFlashAttribute한번쓰고 사라진다.
        return "redirect:" + SETTINGS_PROFILE_URL;
    }

    /** updatePasswordForm
     목적 : 비밀번호 변경 FORM
     설명 : 비밀번호 변경
     비고 :
     **/
    @GetMapping(SETTINGS_PASSWORD_URL)
    public String updatePasswordForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm()); //Profile.class인스턴스가 만들어지고, account정보로 채워진다.
        return SETTINGS_PASSWORD_VIEW_NAME;
    }

    /** updatePassword
     목적 : 비밀번호 변경
     설명 : 비밀번호 변경하고, "패스워드를 변경했습니다." 메세지 표시
     비고 :
     **/
    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:" + SETTINGS_PASSWORD_URL;
    }

    /** updateNotificationsForm
     목적 : 알람 변경 FORM
     설명 : 알람 변경
     비고 :
     **/
    @GetMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS_NOTIFICATIONS_VIEW_NAME;
    }

    /** updateNotifications
     목적 : 알람 변경
     설명 : 알람 변경하고, "알림 설정을 변경했습니다." 메세지 표시
     비고 :
     **/
    @PostMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_NOTIFICATIONS_VIEW_NAME;
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:" + SETTINGS_NOTIFICATIONS_URL;
    }

    /** updateAccountForm
     목적 : 닉네임 변경 FORM
     설명 : 닉네임 업데이트
     비고 :
     **/
    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    /** updateAccount
     목적 : 닉네임 변경
     설명 : 닉네임 변경하고, "닉네임을 수정했습니다." 메세지 표시
     비고 :
     **/
    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }

    /** updateTags
     목적 : 관심주제 카테고리 조회
     설명 : settings/tags 뷰로 이동
     호출 : 카테고리에서 작성했던 키워드 보여준다.
     **/
    @GetMapping(SETTINGS_TAGS_URL)
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList())); //문자열 타입의 리스트로 model담아서 보냄
        //tags라는 컬렉션을 순회하며 각 Tag 객체의 title을 추출하고, 그 결과를 리스트로 변환한 후 모델에 추가

        // 관심 주제 자동완성
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS_TAGS_VIEW_NAME;
    }

    /** addTag
     목적 : 관심주제 키워드 추가
     설명 : "/settings/tags/add"가 아닌 /settings/tags에서 add호출이 따로감
     호출 : 프로필 수정에서 관심주제 키워드 작성시
     **/
    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) { //ajax에서 요청 body로 post전송하기때문에 @RequestBody 애노테이션붙인다.
        /*
        호출 과정

        1. 앞단에서 (tags.html 51lines)
        data: JSON.stringify({'tagTitle': tagTitle}) tagTitle로 요청을 보냄.

        2. 서버에서 tagTitle를 받아줄 객체가 필요하므로, TagForm 데이터 클래스를 만듦.

        3. 컨트롤러에서 @RequestBody TagForm tagForm를 매개변수로 받음

        4. tagForm.getTagTitle()메서드로 앞단에서 보낸 키워드를 title변수에 담음

        5. tagRepository저장소에서 같은 키워드가 있는지 검색

        6. 없으면 저장

         */

        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title); //없으면 null 반환
        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(tagForm.getTagTitle()).build());
        }

        /*
        Q. 빌더 패턴이란?
        A. 빌더 패턴은 복잡한 객체를 생성하는 클래스와 표현하는 클래스를 분리하여, 동일한 절차에서도 서로 다른 표현을 생성하는 방법을 제공

        Q. 빌더 패턴 장점
        A. 필요한 데이터만 설정, 유연성 확보, 가독성, 변경가능성 최소화

        해당 클래스에(Tag) @Builder 애노테이션 붙여야한다.

        참고:https://mangkyu.tistory.com/163
         */

        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    /** removeTag
     목적 : 관심주제 키워드 삭제
     설명 : "/settings/tags/add"가 아닌 /settings/tags에서 remove호출이 따로감
     호출 : 프로필 수정에서 관심주제 키워드 삭제시
     **/
    @PostMapping(SETTINGS_TAGS_URL + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return SETTINGS + ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }
}
