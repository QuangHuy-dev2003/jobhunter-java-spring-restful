package vn.hoidanit.jobhunter.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.DTO.Meta;
import vn.hoidanit.jobhunter.domain.DTO.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {
        return this.userRepository.save(user);
    }

    public void handleDeleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    public User fetchUserByID(Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }

    public ResultPaginationDTO fetchAllUsers(Pageable pageable) {
        Page<User> comPage = this.userRepository.findAll(pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        Meta mt = new Meta();
        mt.setPage(comPage.getNumber());
        mt.setPageSize(comPage.getSize());

        mt.setPages(comPage.getTotalPages());
        mt.setTotal(comPage.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(comPage.getContent());

        return rs;

    }

    public User handleUpdateUser(User user) {
        User currentUser = this.fetchUserByID(user.getId());
        if (currentUser != null) {
            currentUser.setName(user.getName());
            currentUser.setEmail(user.getEmail());
            currentUser.setPassword(user.getPassword());
            return this.userRepository.save(currentUser);
        } else {
            return null;

        }
    }

    public User handleGetUserByUserName(String username) {
        return this.userRepository.findByEmail(username);
    }
}
