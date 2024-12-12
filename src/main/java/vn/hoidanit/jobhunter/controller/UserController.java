package vn.hoidanit.jobhunter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.service.UserService;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/users")
    public User createNewUser(
            @RequestBody User postManUser) {
        
        User user= this.userService.handleCreateUser(postManUser);

        return user;
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        this.userService.handleDeleteUser(id);
        return "User with id: " + id + " has been deleted";
    }

    @GetMapping("/users/{id}")
    public User getUserByID(@PathVariable("id") Long id) {
        return this.userService.fetchUserByID(id);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return this.userService.fetchAllUsers();
    }

    @PutMapping("/users")
    public User updateUser(@RequestBody User user)
    {
        User userUpdate = this.userService.handleUpdateUser(user);
        return userUpdate;
    }

}
