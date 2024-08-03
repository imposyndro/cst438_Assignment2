package com.cst438.controller;

import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/*
 * CRUD APIs for User entity:
 * - List all users
 * - Create a new user
 * - Update user (selected fields: name, email, type)
 * - Delete user
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserRepository userRepository;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<UserDTO> findAllUsers() {
        List<User> users = userRepository.findAllByOrderByIdAsc();
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : users) {
            userDTOList.add(new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getType()));
        }
        return userDTOList;
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.name());
        user.setEmail(userDTO.email());
        String password = userDTO.name() + "2024";
        String encPassword = encoder.encode(password);
        user.setPassword(encPassword);
        user.setType(userDTO.type());
        if (!isValidUserType(userDTO.type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user type");
        }
        userRepository.save(user);
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getType());
    }

    @PutMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public UserDTO updateUser(@RequestBody UserDTO userDTO) {
        User user = userRepository.findById(userDTO.id()).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID not found");
        }
        user.setName(userDTO.name());
        user.setEmail(userDTO.email());
        user.setType(userDTO.type());
        if (!isValidUserType(userDTO.type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user type");
        }
        userRepository.save(user);
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getType());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public void deleteUser(@PathVariable("id") int id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID not found");
        }
        userRepository.delete(user);
    }

    private boolean isValidUserType(String type) {
        return type.equals("STUDENT") || type.equals("INSTRUCTOR") || type.equals("ADMIN");
    }
}
