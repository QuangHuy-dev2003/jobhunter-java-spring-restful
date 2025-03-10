package vn.hoidanit.jobhunter.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.Util.Error.IdInvalidException;
import vn.hoidanit.jobhunter.Util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.reponse.ReqChangePasswordDTO;
import vn.hoidanit.jobhunter.domain.reponse.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.reponse.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.reponse.ResUserDTO;
import vn.hoidanit.jobhunter.domain.reponse.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.request.ResetPasswordDTO;
import vn.hoidanit.jobhunter.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ApiMessage("Create new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User postManUser)
            throws IdInvalidException {
        boolean isEmailExist = this.userService.handleCheckUserExistByEmail(postManUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + postManUser.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");

        }
        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        User user = this.userService.handleCreateUser(postManUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(user));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) throws IdInvalidException {
        User user = this.userService.fetchUserByID(id);
        if (user == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");

        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Get user by id")
    public ResponseEntity<ResUserDTO> getUserByID(@PathVariable("id") Long id) throws IdInvalidException {
        User user = this.userService.fetchUserByID(id);
        if (user == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");

        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResUserDTO(user));
    }

    @GetMapping("/users")
    @ApiMessage("Get all users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchAllUsers(spec, pageable));
    }

    @PutMapping("/users")
    @ApiMessage("Update user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user) throws IdInvalidException {
        User userUpdate = this.userService.handleUpdateUser(user);
        if (userUpdate == null) {
            throw new IdInvalidException("User với id = " + user.getId() + " không tồn tại");

        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResUpdateUserDTO(userUpdate));
    }

    @PutMapping("/users/{id}/change-password")
    @ApiMessage("Change user password")
    public ResponseEntity<?> changePassword(
            @PathVariable("id") long id,
            @Valid @RequestBody ReqChangePasswordDTO reqChangePassword) throws IdInvalidException {

        User user = this.userService.handleChangePassword(
                id,
                reqChangePassword.getCurrentPassword(),
                reqChangePassword.getNewPassword());

        if (user == null) {
            throw new IdInvalidException("Mật khẩu hiện tại không chính xác");
        }

        return ResponseEntity.ok().body(Map.of(
                "message", "Thay đổi mật khẩu thành công"));
    }

    @PostMapping("/users/reset-password")
    @ApiMessage("Reset password with email")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO request) {
        try {
            this.userService.resetPassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok().body(new HashMap<String, String>() {{
                put("message", "Đổi mật khẩu thành công");
            }});
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<String, String>() {{
                put("message", e.getMessage());
            }});
        }
    }
}
