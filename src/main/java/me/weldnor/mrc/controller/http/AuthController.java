package me.weldnor.mrc.controller.http;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.dto.login.LoginRequestDto;
import me.weldnor.mrc.domain.dto.user.NewUserDto;
import me.weldnor.mrc.domain.dto.user.UserDto;
import me.weldnor.mrc.exception.auth.IncorrectPasswordException;
import me.weldnor.mrc.exception.user.UserAlreadyExistsException;
import me.weldnor.mrc.exception.user.UserNotFoundException;
import me.weldnor.mrc.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UserDto login(@RequestBody LoginRequestDto loginRequestDto) throws UserNotFoundException, IncorrectPasswordException {
        return authService.login(loginRequestDto);
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody NewUserDto newUserDto) throws UserAlreadyExistsException {
        return authService.register(newUserDto);
    }
}
