package com.michelin.connectedfleet.ELD_Backend;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.michelin.connectedfleet.ELD_Backend.data.CreateUserRequest;
import com.michelin.connectedfleet.ELD_Backend.data.User;
import com.michelin.connectedfleet.ELD_Backend.data.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @PostMapping
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        String passwordHash = passwordEncoder.encode(createUserRequest.password());
        User newUser = new User(createUserRequest.username(), passwordHash, createUserRequest.firstName(), createUserRequest.lastName());
        userRepository.insert(newUser);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
