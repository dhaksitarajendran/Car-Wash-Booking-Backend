package com.kce.AuthService.repository;

import com.kce.AuthService.entity.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    List<Address> findByUserId(Long userId);
}
