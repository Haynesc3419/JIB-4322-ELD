package com.michelin.connectedfleet.ELD_Backend;

import com.michelin.connectedfleet.ELD_Backend.data.*;
import com.mongodb.MongoWriteException;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        String passwordHash = passwordEncoder.encode(createUserRequest.password());
        User newUser = new User(createUserRequest.username(), passwordHash, createUserRequest.firstName(), createUserRequest.lastName());
        try {
            userRepository.insert(newUser);
        } catch (DuplicateKeyException e) {
            return new ResponseEntity<>(new ErrorResponse("Username already exists"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> login(@Valid @RequestBody LoginUserRequest loginUserRequest) {
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(loginUserRequest.username(),
                        loginUserRequest.password());
        try {
            Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String token = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        LoginUserResponse response = new LoginUserResponse(loginUserRequest.username());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
