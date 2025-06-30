package com.torah.torahAI.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.torah.torahAI.data.UserRepo;
import com.torah.torahAI.data.documents.User;
import com.torah.torahAI.model.SignUp;

import at.favre.lib.crypto.bcrypt.BCrypt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class SignUpController {

    private UserRepo userRepo;

    @Autowired
    public SignUpController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
    
    @PostMapping(value="/user/auth/signup")
    public ResponseEntity<String> postMethodName(@RequestBody SignUp signUpRequest) {
        if(userRepo.findByUsername(signUpRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        String hash = BCrypt.withDefaults().hashToString(12, signUpRequest.getPassword().toCharArray());

        User user = new User();
        user.setPasswordHash(hash);
        user.setUsername(signUpRequest.getUsername());
        userRepo.save(user);
        return ResponseEntity.ok("User created successfully");
    }
    
}
