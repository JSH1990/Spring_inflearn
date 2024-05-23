package com.studyolle.settings;

import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import com.studyolle.settings.form.NicknameForm;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.PasswordForm;
import com.studyolle.settings.form.Profile;
import com.studyolle.settings.validator.NicknameValidator;
import com.studyolle.settings.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

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

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;

    /** initBinder
     목적 : 데이터 바인딩 및 검증
     설명 : passwordForm과 관련된 요청이 있을 때마다 PasswordFormValidator를 사용하여 검증을 한다.
     비고 : @initBinder이란? 특정 컨트롤러에서 입력 데이터의 변환, 검증 등을 설정하는 메서드에 적용
     **/
    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) { //PasswordFormValidator 인스턴스를 WebDataBinder에 추가하여, passwordForm 객체에 대한 검증을 수행하도록 설정
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    /** profileUpdateForm
     목적 : 프로필 설정 폼
     설명 : 현재 접속해 있는 @CurrentUser account객체와 프로필 설정 new Profile(account) 객체를 model에 담아 뷰로 보낸다.
     비고 :
     **/
    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Profile(account));

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
}
