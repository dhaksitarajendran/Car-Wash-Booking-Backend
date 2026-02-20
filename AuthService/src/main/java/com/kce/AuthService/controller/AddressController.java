package com.kce.AuthService.controller;

import com.kce.AuthService.entity.Address;
import com.kce.AuthService.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/address")
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend access
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("/add")
    public Address addAddress(@RequestBody Address address) {
        return addressService.addAddress(address);
    }

    @GetMapping("/my")
    public List<Address> getMyAddresses() {
        return addressService.getMyAddresses();
    }

    @GetMapping("/all")
    public List<Address> getAllAddresses() {
        return addressService.getAllAddresses();
    }

    @GetMapping("/{id}")
    public Address getAddressById(@PathVariable String id) {
        return addressService.getAddressById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteAddress(@PathVariable String id) {
        addressService.deleteAddress(id);
    }
}
