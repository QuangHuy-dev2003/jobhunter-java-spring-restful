package vn.hoidanit.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.Util.SecurityUtil;
import vn.hoidanit.jobhunter.Util.Error.IdInvalidException;
import vn.hoidanit.jobhunter.Util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.reponse.ResLoginDTO;
import vn.hoidanit.jobhunter.domain.request.ReqLoginDTO;
import vn.hoidanit.jobhunter.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;

        @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
                        SecurityUtil securityUtil,
                        UserService userService) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
        }

        @PostMapping("/auth/login")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {

                // Nạp input gồm username/password vào Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());
                // xác thực người dùng => cần viết hàm loadUserByUsername
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);

                // set information to SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // create token

                ResLoginDTO resLoginDTO = new ResLoginDTO();
                User currentUserDb = this.userService.handleGetUserByUserName(loginDTO.getUsername());
                if (currentUserDb != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        currentUserDb.getId(),
                                        currentUserDb.getName(),
                                        currentUserDb.getEmail());
                        resLoginDTO.setUser(userLogin);
                }
                String access_token = this.securityUtil.createAccessToken(authentication.getName(),
                                resLoginDTO.getUser());
                resLoginDTO.setAccessToken(access_token);

                // create refresh token
                String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), resLoginDTO);
                // update refresh token to user
                this.userService.updateUserToken(refresh_token, loginDTO.getUsername());
                // set cookie
                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token", refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(org.springframework.http.HttpHeaders.SET_COOKIE,
                                                resCookies.toString())
                                .body(resLoginDTO);
        }

        @GetMapping("/auth/account")
        @ApiMessage("Account")
        public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                User currentUserDb = this.userService.handleGetUserByUserName(email);
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
                ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
                if (currentUserDb != null) {
                        userLogin.setId(currentUserDb.getId());
                        userLogin.setUsername(currentUserDb.getName());
                        userLogin.setEmail(currentUserDb.getEmail());
                        userGetAccount.setUser(userLogin);

                }
                return ResponseEntity.ok().body(userGetAccount);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("Refresh token")
        public ResponseEntity<ResLoginDTO> getRefreshToken(
                        @CookieValue(name = "refresh_token") String refresh_token)
                        throws Exception {

                // check valid
                Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
                // get email
                String email = decodedToken.getSubject();
                // check user by token + email
                User currentUser = this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
                if (currentUser == null) {
                        throw new IdInvalidException("Refresh token không hợp lệ");
                }
                ResLoginDTO resLoginDTO = new ResLoginDTO();
                User currentUserDb = this.userService.handleGetUserByUserName(email);
                if (currentUserDb != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        currentUserDb.getId(),
                                        currentUserDb.getName(),
                                        currentUserDb.getEmail());
                        resLoginDTO.setUser(userLogin);
                }
                String access_token = this.securityUtil.createAccessToken(email, resLoginDTO.getUser());
                resLoginDTO.setAccessToken(access_token);

                // create refresh token
                String new_refresh_token = this.securityUtil.createRefreshToken(email, resLoginDTO);
                // update refresh token to user
                this.userService.updateUserToken(new_refresh_token, email);
                // set cookie
                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token",
                                                new_refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(org.springframework.http.HttpHeaders.SET_COOKIE,
                                                resCookies.toString())
                                .body(resLoginDTO);
        }

        @PostMapping("/auth/logout")
        @ApiMessage("Logout")
        public ResponseEntity<String> logout() throws Exception {
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                if (email.equals("")) {
                        throw new IdInvalidException("Access Token không hợp lệ");
                }
                this.userService.deleteRefreshToken(email);
                ResponseCookie deleteResCookies = ResponseCookie
                                .from("refresh_token", "")
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();
                return ResponseEntity.ok()
                                .header(org.springframework.http.HttpHeaders.SET_COOKIE,
                                                deleteResCookies.toString())
                                .body("Logout success");
        }
}
