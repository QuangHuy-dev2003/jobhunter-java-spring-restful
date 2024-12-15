package vn.hoidanit.jobhunter.controller;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.Util.Error.IdInvalidException;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.DTO.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.UserService;

@RestController
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createNewUser(
            @RequestBody User postManUser) {
        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        User user = this.userService.handleCreateUser(postManUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) throws IdInvalidException {
        if (id >= 1500) {
            throw new IdInvalidException("Id is invalid");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("User with id: " + id + " has been deleted");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserByID(@PathVariable("id") Long id) {
        User user = this.userService.fetchUserByID(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @RequestParam("current") Optional<String> currentOptional,
            @RequestParam("pageSize") Optional<String> pageSizeOptional) {
        String sCurrent = currentOptional.isPresent() ? currentOptional.get() : "";
        String sPageSize = pageSizeOptional.isPresent() ? pageSizeOptional.get() : "";
        int current = Integer.parseInt(sCurrent);
        int pageSize = Integer.parseInt(sPageSize);
        Pageable pageable = PageRequest.of(current - 1, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchAllUsers(pageable));
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        User userUpdate = this.userService.handleUpdateUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(userUpdate);
    }

}
