package vn.hoidanit.jobhunter.controller;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.ResetPasswordDTO;
import vn.hoidanit.jobhunter.domain.response.ReqChangePasswordDTO;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.service.CloudinaryService;
import vn.hoidanit.jobhunter.service.RoleService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleService roleService;

    public UserController(UserService userService, PasswordEncoder passwordEncoder,
            CloudinaryService cloudinaryService,
            UserRepository userRepository,
            RoleService roleService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
        this.roleService = roleService;
        this.userRepository = userRepository;

    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User postManUser)
            throws IdInvalidException {
        boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + postManUser.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
        }

        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        User ericUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(ericUser));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id)
            throws IdInvalidException {
        User currentUser = this.userService.fetchUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("fetch user by id")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id) throws IdInvalidException {
        User fetchUser = this.userService.fetchUserById(id);
        if (fetchUser == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.userService.convertToResUserDTO(fetchUser));
    }

    // fetch all users
    @GetMapping("/users")
    @ApiMessage("fetch all users")
    public ResponseEntity<ResultPaginationDTO> getAllUser(
            @Filter Specification<User> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.fetchAllUser(spec, pageable));
    }

    @PutMapping("/users/update")
    @ApiMessage("Update user info")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user)
            throws IdInvalidException {
        User updatedUser = this.userService.handleUpdateUser(user);
        if (updatedUser == null) {
            throw new IdInvalidException("User với id = " + user.getId() + " không tồn tại");
        }
        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(updatedUser));
    }

    @PutMapping("/users/{userId}/profile-image")
    @ApiMessage("Update user profile image")
    public ResponseEntity<ResUpdateUserDTO> updateProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) throws IdInvalidException, IOException {
        User user = this.userService.fetchUserById(userId);
        if (user == null) {
            throw new IdInvalidException("User với id = " + userId + " không tồn tại");
        }

        if (file != null && !file.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (user.getUrlProfile() != null && !user.getUrlProfile().isEmpty()) {
                String publicId = user.getUrlProfile().substring(
                        user.getUrlProfile().lastIndexOf('/') + 1,
                        user.getUrlProfile().lastIndexOf('.'));
                cloudinaryService.deleteFile(publicId);
            }

            // Tải lên ảnh mới
            String imageUrl = cloudinaryService.uploadFile(file, "users/avatars", "avatar_");
            user.setUrlAvatar(imageUrl);
            user = this.userRepository.save(user);
        }

        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(user));
    }

    @DeleteMapping("/users/{userId}/profile-image")
    @ApiMessage("Delete user profile image")
    public ResponseEntity<?> deleteProfileImage(@PathVariable Long userId) throws IdInvalidException, IOException {
        User user = this.userService.fetchUserById(userId);
        if (user == null) {
            throw new IdInvalidException("User với id = " + userId + " không tồn tại");
        }

        if (user.getUrlAvatar() != null && !user.getUrlAvatar().isEmpty()) {
            String publicId = this.cloudinaryService.extractPublicId(user.getUrlAvatar());
            this.cloudinaryService.deleteFile(publicId);

            // Cập nhật user để xóa URL ảnh
            user.setUrlAvatar(null);
            user = this.userRepository.save(user);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Xóa ảnh đại diện thành công",
                    "user", this.userService.convertToResUpdateUserDTO(user)));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Người dùng không có ảnh đại diện để xóa"));
        }
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
            return ResponseEntity.ok().body(new HashMap<String, String>() {
                {
                    put("message", "Đổi mật khẩu thành công");
                }
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<String, String>() {
                {
                    put("message", e.getMessage());
                }
            });
        }
    }

    @GetMapping("/users/post_count/{id}")
    @ApiMessage("fetch post_count by user id")
    public ResponseEntity<Long> getPostCountByUserId(@PathVariable("id") long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getPostCountByUserId(id));
    }

    // API count User
    @GetMapping("/users/count")
    @ApiMessage("fetch total user count")
    public ResponseEntity<Long> getTotalUserCount() {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getTotalUserCount());
    }

    @GetMapping("/users/me")
    @ApiMessage("Lấy thông tin người dùng hiện tại")
    public ResponseEntity<ResUserDTO> getCurrentUser() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        ResUserDTO resUserDTO = userService.convertToResUserDTO(user);
        return ResponseEntity.ok(resUserDTO);
    }

    @PutMapping("/users/{userId}/assign-role/{roleId}")
    @ApiMessage("Assign role to user")
    public ResponseEntity<?> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) throws IdInvalidException {

        User user = userService.fetchUserById(userId);
        if (user == null) {
            throw new IdInvalidException("User với id = " + userId + " không tồn tại");
        }

        Role role = roleService.fetchById(roleId);
        if (role == null) {
            throw new IdInvalidException("Role với id = " + roleId + " không tồn tại");
        }

        user.setRole(role);
        // Nếu role là HR, mặc định isHrActivated = false
        if (role.getName().equals("HR")) {
            user.setIsHrActivated(false);
        }

        userRepository.save(user);
        return ResponseEntity.ok().body(Map.of(
                "message", "Gán role thành công"));
    }

    // Check HR kích hoạt hay chưa
    @GetMapping("/users/me/hr-status")
    @ApiMessage("Kiểm tra trạng thái kích hoạt của HR")
    public ResponseEntity<?> checkHrActivationStatus() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Không tìm thấy thông tin đăng nhập"));
        }

        User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Kiểm tra xem người dùng có phải là HR không
        boolean isHrRole = user.getRole() != null && user.getRole().getName().equals("HR");

        // Tạo response object
        Map<String, Object> response = new HashMap<>();
        response.put("isHrRole", isHrRole);

        // Nếu là HR, kiểm tra trạng thái kích hoạt
        if (isHrRole) {
            boolean isActivated = user.getIsHrActivated() != null ? user.getIsHrActivated() : false;
            response.put("isHrActivated", isActivated);

            if (!isActivated) {
                response.put("message", "Tài khoản HR của bạn chưa được kích hoạt. Vui lòng liên hệ quản trị viên.");
            } else {
                response.put("message", "Tài khoản HR của bạn đã được kích hoạt.");
            }
        } else {
            response.put("message", "Người dùng không có vai trò HR.");
        }

        return ResponseEntity.ok(response);
    }

}
