package vn.hoidanit.jobhunter.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.Util.SecurityUtil;
import vn.hoidanit.jobhunter.Util.Error.IdInvalidException;
import vn.hoidanit.jobhunter.Util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.reponse.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.reponse.ResLoginDTO;
import vn.hoidanit.jobhunter.domain.request.ReqLoginDTO;
import vn.hoidanit.jobhunter.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;
        private final PasswordEncoder passwordEncoder;

        @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
                        SecurityUtil securityUtil,
                        UserService userService,
                        PasswordEncoder passwordEncoder) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
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
                                        currentUserDb.getEmail(),
                                        currentUserDb.getRole());
                        resLoginDTO.setUser(userLogin);
                }
                // create access token
                String access_token = this.securityUtil.createAccessToken(authentication.getName(),
                                resLoginDTO);
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

        @GetMapping("/auth/google-success")
        public ResponseEntity<?> googleLoginSuccess(HttpServletResponse response,
                        @RequestParam String email) throws IOException {
                try {

                        // Lấy thông tin user từ database
                        User currentUserDB = this.userService.handleGetUserByUserName(email);
                        if (currentUserDB == null) {
                                Map<String, Object> errorResponse = new HashMap<>();
                                errorResponse.put("statusCode", 404);
                                errorResponse.put("message", "User not found");
                                errorResponse.put("error", "Not Found");
                                errorResponse.put("data", null);

                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(errorResponse);
                        }

                        // Tạo response object
                        ResLoginDTO res = new ResLoginDTO();
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName(),
                                        currentUserDB.getRole());
                        res.setUser(userLogin);

                        // Tạo token
                        String access_token = this.securityUtil.createAccessToken(email, res);
                        res.setAccessToken(access_token);
                        String refresh_token = this.securityUtil.createRefreshToken(email, res);

                        // Cập nhật database
                        this.userService.updateUserToken(refresh_token, email);

                        // Tạo cookie
                        ResponseCookie resCookies = ResponseCookie
                                        .from("refresh_token", refresh_token)
                                        .httpOnly(true)
                                        .secure(true)
                                        .path("/")
                                        .maxAge(refreshTokenExpiration)
                                        .build();

                        // Redirect về frontend
                        response.sendRedirect(
                                        String.format("http://localhost:3000/auth/google-callback?access_token=%s",
                                                        access_token));

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                        .body(res);

                } catch (Exception e) {
                        e.printStackTrace();
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("statusCode", 500);
                        errorResponse.put("message", "Login failed: " + e.getMessage());
                        errorResponse.put("error", "Internal Server Error");
                        errorResponse.put("data", null);

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(errorResponse);
                }
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
                        userLogin.setRole(currentUserDb.getRole());
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
                                        currentUserDb.getEmail(),
                                        currentUserDb.getRole());
                        resLoginDTO.setUser(userLogin);
                }
                String access_token = this.securityUtil.createAccessToken(email, resLoginDTO);
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

        @PostMapping("/auth/register")
        @ApiMessage("Register a new user")
        public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody User postManUser)
                        throws IdInvalidException {
                boolean isEmailExist = this.userService.handleCheckUserExistByEmail(postManUser.getEmail());
                if (isEmailExist) {
                        throw new IdInvalidException(
                                        "Email " + postManUser.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
                }
                String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
                postManUser.setPassword(hashPassword);
                User ericUser = this.userService.handleCreateUser(postManUser);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(this.userService.convertToResCreateUserDTO(ericUser));
        }
}
