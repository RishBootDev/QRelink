package org.rishbootdev.qrelink.controller;


import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.rishbootdev.qrelink.dtos.LoginRequest;
import org.rishbootdev.qrelink.dtos.RegisterRequest;
import org.rishbootdev.qrelink.models.User;
import org.rishbootdev.qrelink.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private UserService userService;
    private ModelMapper modelMapper;

    @PostMapping("/public/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }

    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest){

        User user=modelMapper.map(registerRequest,User.class);
        user.setRole("ROLE_USER");
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }
}
