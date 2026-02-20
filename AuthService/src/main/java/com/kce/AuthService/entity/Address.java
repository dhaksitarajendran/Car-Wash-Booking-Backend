package com.kce.AuthService.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    private String id;

    // Link to User (using Long userId from User entity)
    private Long userId;

    private String name;

    private String address;

    private String phone;
}
