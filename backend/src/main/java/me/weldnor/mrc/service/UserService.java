package me.weldnor.mrc.service;

import me.weldnor.mrc.dto.login.LoginRequestDto;
import me.weldnor.mrc.dto.password.UpdatePasswordDto;
import me.weldnor.mrc.dto.user.NewUserDto;
import me.weldnor.mrc.dto.user.UpdateUserDto;
import me.weldnor.mrc.dto.user.UserDto;
import me.weldnor.mrc.exception.user.UserNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto addUser(NewUserDto user);

    void deleteAllUsers();

    UserDto getUser(long userId) throws UserNotFoundException;

    void updateUser(long userId, UpdateUserDto updateUserDto) throws UserNotFoundException;

    void deleteUser(long userId);

    boolean updateUserPassword(long userId, UpdatePasswordDto updatePasswordDto) throws UserNotFoundException;

    Optional<UserDto> login(LoginRequestDto loginRequestDto);

    UserDto register(NewUserDto user);
}
