package com.backend.controller;


import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @GetMapping("/users")
    public String getUsers(){
      return  "Response from backend service";
    }

    @PostMapping("/users")
    public String createUser(@RequestBody String requestBody,@RequestHeader("X-Request-Source") String requestSource) {
        return "Backend received:" + requestBody  + " | Source: " + requestSource;
    }
    @PutMapping("/users")
    public String updateUser(@RequestBody String requestBody) {
        return "Backend updated: " + requestBody;
    }
    @DeleteMapping("/users")
    public String deleteUser() {
        return "Backend user deleted successfully";
    }
}
