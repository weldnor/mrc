package me.weldnor.mrc.controller.http;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.dto.login.LoginRequestDto;
import me.weldnor.mrc.domain.dto.user.NewUserDto;
import me.weldnor.mrc.domain.dto.user.UserDto;
import me.weldnor.mrc.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/public")
@Slf4j
public class PublicController {
    private final UserService userService;

    public PublicController(UserService userService) {
        this.userService = userService;
    }

    // FIXME
    @PostMapping("/login")
    public UserDto login(@RequestBody LoginRequestDto loginRequestDto) {
        return userService.login(loginRequestDto).orElseThrow();
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody NewUserDto newUserDto) {
        return userService.register(newUserDto);
    }
}
