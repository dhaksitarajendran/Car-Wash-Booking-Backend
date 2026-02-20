package com.kce.AuthService.repository;

import com.kce.AuthService.entity.User;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);
    Optional<User> findByPhoneNo(String phoneNo);
    boolean existsByPhoneNo(String phoneNo);
    
}