package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.DTO.Meta;
import vn.hoidanit.jobhunter.domain.DTO.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.DTO.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.DTO.ResUserDTO;
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

    public ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> comPage = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(comPage.getTotalPages());
        mt.setTotal(comPage.getTotalElements());

        rs.setMeta(mt);
        // remove sensitive data
        List<ResUserDTO> listUser = comPage.getContent()
                .stream().map(item -> new ResUserDTO(
                        item.getId(),
                        item.getEmail(),
                        item.getName(),
                        item.getGender(),
                        item.getAddress(),
                        item.getAge(),
                        item.getUpdatedAt(),
                        item.getCreatedAt()))
                .collect(Collectors.toList());

        rs.setResult(listUser);

        return rs;

    }

    public User handleUpdateUser(User user) {
        User currentUser = this.fetchUserByID(user.getId());
        if (currentUser != null) {
            currentUser.setName(user.getName());
            currentUser.setAge(user.getAge());
            currentUser.setGender(user.getGender());
            currentUser.setAddress(user.getAddress());

            return this.userRepository.save(currentUser);
        } else {
            return currentUser;

        }
    }

    public User handleGetUserByUserName(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean handleCheckUserExistByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        return res;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        return res;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setCreatedAt(user.getCreatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        return res;
    }

    public void updateUserToken(String token, String email){
        User currentUser = this.handleGetUserByUserName(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }
}
