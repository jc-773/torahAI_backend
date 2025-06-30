package com.torah.torahAI.data;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.torah.torahAI.data.documents.User;

public interface UserRepo extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}
