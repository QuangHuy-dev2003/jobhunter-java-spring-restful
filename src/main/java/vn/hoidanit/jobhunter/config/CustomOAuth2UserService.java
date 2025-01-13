package vn.hoidanit.jobhunter.config;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.Util.constant.GenderEnum;
import vn.hoidanit.jobhunter.Util.constant.ProviderEnum;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.service.UserService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserService userService,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // call api
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Kiểm tra xem email đã tồn tại chưa
        User user = userService.handleGetUserByUserName(email);
        if (user == null) {
            // Tạo user mới
            user = new User();
            user.setEmail(email);
            user.setName(name);
            // Mã hóa mật khẩu mặc định "123456"
            String encodedPassword = passwordEncoder.encode("123456"); // Mã hóa mật khẩu
            user.setPassword(encodedPassword); // Gán mật khẩu đã mã hóa

            // Set một số thông tin mặc định
            user.setAge(0);
            user.setGender(GenderEnum.MALE);
            user.setAddress("");
            if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
                user.setProvider(ProviderEnum.GOOGLE);
            } else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
                user.setProvider(ProviderEnum.FACEBOOK);
            }

            // Role là null
            user.setRole(null);

            userService.handleCreateUser(user);
        }

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "email");
    }
}
