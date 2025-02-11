package vn.hoidanit.jobhunter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
            "/", "/api/v1/auth/**", "/storage/**",
            "/api/v1/companies/**", "/api/v1/jobs/**", "/api/v1/skills/**", "/api/v1/files",
            "/api/v1/resumes/**",
            "/api/v1/subscribers/**",
            "/api/v1/users//post-limits/**",
            "/api/v1/users/{id}/change-password",
            "/api/v1/users/{id}",
            "/api/v1/users/post_count/{id}",
            "/api/v1/users/update",
            "/api/v1/users/{userId}/profile-image",
            "/api/v1/post-limits/**",
            "/api/v1/payments/**",
            "/api/v1/subscriptions/**",
            "/api/v1/user-saved-jobs/**",



        };
        registry.addInterceptor(getPermissionInterceptor())
            .excludePathPatterns(whiteList);
    }
}