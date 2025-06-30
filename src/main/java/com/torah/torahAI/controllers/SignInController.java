package com.torah.torahAI.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.torah.torahAI.JwtUtils;
import com.torah.torahAI.data.UserRepo;
import com.torah.torahAI.data.documents.User;
import com.torah.torahAI.model.SignUp;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class SignInController {
    
    private JwtUtils jwtUtils;
    private UserRepo userRepo;

    @Autowired
    public SignInController(JwtUtils jwtUtils, UserRepo userRepo) {
        this.jwtUtils = jwtUtils;
        this.userRepo = userRepo;
    }

    @GetMapping(value="/user/auth/signin")
    public ResponseEntity<String> getValidJwt(@RequestBody SignUp signInRequest) {
        //check if email exist by using optional var is empty
        //if it does exist store return val as new User
        //use this code to map the hash to the password:  BCrypt.Result  result = BCrypt.verifyer().verify(userRequest.getPassword().toCharArray(), user.getPasswordHash());
        //if result is not verified - invalid
        //otherwise generate token from jwtUtils with email from request

        Optional<User> userOpt = userRepo.findByUsername(signInRequest.getUsername());
        if(!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        User user = userOpt.get();
        BCrypt.Result result = BCrypt.verifyer().verify(signInRequest.getPassword().toCharArray(), user.getPasswordHash());

        if(!result.verified) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        String jwt = jwtUtils.generateToken(signInRequest.getUsername());
        return ResponseEntity.ok(jwt);
    }
    
}
