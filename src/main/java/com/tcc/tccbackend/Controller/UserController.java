package com.tcc.tccbackend.Controller;

import com.tcc.tccbackend.DTO.LoginDTO;
import com.tcc.tccbackend.Model.User;
import com.tcc.tccbackend.Service.UserService;
import com.tcc.tccbackend.DTO.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Iterable<User>> getAllUsers() {
        Iterable<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody @Valid UserDTO user) {
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<User> Login(@RequestBody LoginDTO loginDTO){
        User newUser = userService.Login(loginDTO);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @GetMapping("/{email}")
    public Optional<User> getUserByEmail(@PathVariable String email) {
        return userService.findUserByEmail(email);
    }
}