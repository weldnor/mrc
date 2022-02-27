package me.weldnor.mrc.controller.http;


import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.dto.user.UserDto;
import me.weldnor.mrc.exception.user.UserNotFoundException;
import me.weldnor.mrc.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping
    public void deleteAllUsers() {
        userService.deleteAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable ObjectId userId) throws UserNotFoundException {
        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable ObjectId userId) {
        userService.deleteUser(userId);
    }


//    @PutMapping("/{userId}/password")
//    public boolean updateUserPassword(@PathVariable long userId, @RequestBody UpdatePasswordDto updatePasswordDto) throws UserNotFoundException {
//        return userService.updateUserPassword(userId, updatePasswordDto);
//    }
}
