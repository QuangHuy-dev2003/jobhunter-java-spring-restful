package vn.hoidanit.jobhunter.domain.reponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.jobhunter.domain.Role;

@Getter
@Setter
public class ResLoginDTO {
    @JsonProperty("access_token")
    private String accessToken;
    private UserLogin user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLogin {
        private long id;
        private String username;
        private String email;
        private Role role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGetAccount {
        private UserLogin user;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInsideToken {
        private long id;
        private String email;
        private String name;
    }

}
