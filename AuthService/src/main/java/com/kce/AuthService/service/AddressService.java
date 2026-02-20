package com.kce.AuthService.service;

import com.kce.AuthService.entity.Address;
import com.kce.AuthService.entity.User;
import com.kce.AuthService.repository.AddressRepository;
import com.kce.AuthService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public Address addAddress(Address address) {
        // Assuming security context stores the email/username as principal
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        // Assuming User has getUserId()
        address.setUserId(user.getUserId());
        return addressRepository.save(address);
    }

    public List<Address> getMyAddresses() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        return addressRepository.findByUserId(user.getUserId());
    }

    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    public Address getAddressById(String id) {
        return addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
    }

    // Admin or User can delete? Assuming user can only delete "their" address if
    // check added
    public void deleteAddress(String id) {
        addressRepository.deleteById(id);
    }
}
