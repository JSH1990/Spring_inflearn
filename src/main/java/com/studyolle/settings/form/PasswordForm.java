package com.studyolle.settings.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/** PasswordForm **/
@Data
public class PasswordForm {

    @Length(min=8, max=50)
    private String newPassword;

    @Length(min=8, max=50)
    private String newPasswordConfirm;
}
