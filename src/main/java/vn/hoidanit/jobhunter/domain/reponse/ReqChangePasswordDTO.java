package vn.hoidanit.jobhunter.domain.reponse;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqChangePasswordDTO {
    @NotEmpty(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @NotEmpty(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}
