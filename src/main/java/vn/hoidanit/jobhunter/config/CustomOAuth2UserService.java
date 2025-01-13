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
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String name;
        String provider = userRequest.getClientRegistration().getRegistrationId();

        if ("google".equals(provider)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if ("facebook".equals(provider)) {
            // Facebook trả về email trong attributes
            email = (String) attributes.get("email");

            // Facebook trả về name trong key "name"
            name = (String) attributes.get("name");

            // Nếu không có email (Facebook có thể không trả về email)
            if (email == null) {
                // Có thể sử dụng ID của Facebook làm email
                String facebookId = (String) attributes.get("id");
                email = facebookId + "@facebook.com";
            }
        } else {
            throw new OAuth2AuthenticationException("Login provider not supported: " + provider);
        }

        // Log để debug
        System.out.println("Facebook Raw Data:");
        System.out.println("Data Provider: " + provider);
        System.out.println("Data Attributes: " + attributes);
        System.out.println("Data Email: " + email);
        System.out.println("Data Name: " + name);

        // Kiểm tra và tạo user
        User user = userService.handleGetUserByUsername(email);
        if (user != null) {
            // Nếu người dùng đã tồn tại, kiểm tra và cập nhật provider nếu khác nhau
            if ("facebook".equals(provider) && !ProviderEnum.FACEBOOK.equals(user.getProvider())) {
                // Nếu hiện tại người dùng có provider khác, cập nhật từ provider cũ sang
                // Facebook
                ProviderEnum currentProvider = user.getProvider();
                user.setProvider(ProviderEnum.FACEBOOK);
                userService.handleCreateUser(user);
                System.out.println("Updated user provider from " + currentProvider + " to FACEBOOK");
            } else if ("google".equals(provider) && !ProviderEnum.GOOGLE.equals(user.getProvider())) {
                // Nếu hiện tại người dùng có provider khác, cập nhật từ provider cũ sang Google
                ProviderEnum currentProvider = user.getProvider();
                user.setProvider(ProviderEnum.GOOGLE);
                userService.handleCreateUser(user);
                System.out.println("Updated user provider from " + currentProvider + " to GOOGLE");
            }
        } else {
            // Nếu người dùng chưa tồn tại, tạo mới
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setAge(18);
            user.setGender(GenderEnum.MALE);
            user.setAddress("");
            user.setProvider("google".equals(provider) ? ProviderEnum.GOOGLE : ProviderEnum.FACEBOOK);
            System.out.println("Provider: " + user.getProvider());
            System.out.println("Provider ordinal value: " + user.getProvider().ordinal());

            user.setRole(null);
            userService.handleCreateUser(user);
        }

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "google".equals(provider) ? "email" : "id" // Facebook sử dụng "id" làm nameAttributeKey
        );
    }
}
