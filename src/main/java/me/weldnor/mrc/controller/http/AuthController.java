package me.weldnor.mrc.controller.http;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@Slf4j
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

//    @PostMapping("/login")
//    public UserDto login(@RequestBody LoginRequestDto loginRequestDto) {
//        return userService.login(loginRequestDto).orElseThrow();
//    }

//    @PostMapping("/register")
//    public UserDto register(@RequestBody NewUserDto newUserDto) {
//        return userService.register(newUserDto);
//    }
}
