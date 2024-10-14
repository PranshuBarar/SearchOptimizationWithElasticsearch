package com.searchoptimizationv2.search_optimization_application_v2.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.searchoptimizationv2.search_optimization_application_v2.DTOs.UserDTO;
import com.searchoptimizationv2.search_optimization_application_v2.DTOs.UserPasswordUpdateRequest;
import com.searchoptimizationv2.search_optimization_application_v2.Service.UserService;
import com.searchoptimizationv2.search_optimization_application_v2.entities.User;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/hello")
    public String hello() {
        return "Hi";
    }

    @PutMapping("/createUser")
    public String createUser(@RequestBody UserDTO userDTO) throws IOException {
        return userService.createUser(userDTO);
    }

    @PutMapping("/updatePassword")
    public String updateUserPassword(@RequestBody UserPasswordUpdateRequest userPasswordUpdateRequest) throws IOException {
        return userService.updateUserPassword(userPasswordUpdateRequest);
    }

    @DeleteMapping("/deleteUser")
    public String deleteUser(@JsonProperty("userIdString") String userIdString) throws IOException {
        return userService.deleteUser(userIdString);
    }

    @GetMapping("/getUser")
    public User getUser(@JsonProperty("userId") String userIdString){
        return userService.getUser(userIdString);
    }


}
