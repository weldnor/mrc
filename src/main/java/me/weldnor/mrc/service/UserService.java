package me.weldnor.mrc.service;


import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.dto.login.LoginRequestDto;
import me.weldnor.mrc.dto.password.UpdatePasswordDto;
import me.weldnor.mrc.dto.user.NewUserDto;
import me.weldnor.mrc.dto.user.UpdateUserDto;
import me.weldnor.mrc.dto.user.UserDto;
import me.weldnor.mrc.entity.User;
import me.weldnor.mrc.entity.UserPassword;
import me.weldnor.mrc.exception.user.UserNotFoundException;
import me.weldnor.mrc.mapper.UserMapper;
import me.weldnor.mrc.repository.GlobalRoleRepository;
import me.weldnor.mrc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class UserService {

    private UserRepository userRepository;
    private GlobalRoleRepository globalRoleRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;


    public List<UserDto> getAllUsers() {
        var users = userRepository.findAll();
        return userMapper.mapToDto(users);
    }

    public UserDto addUser(NewUserDto dto) {
        User user = userMapper.mapToEntity(dto);
        UserPassword userPassword = new UserPassword();

        String passwordHash = passwordEncoder.encode(dto.getPassword());
        userPassword.setPasswordHash(passwordHash);

        user.setPassword(userPassword);
        userPassword.setUser(user);

        user = userRepository.save(user);
        return userMapper.mapToDto(user);
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    public UserDto getUser(long userId) throws UserNotFoundException {
        User user = findUserById(userId);
        return userMapper.mapToDto(user);
    }

    public void updateUser(long userId, UpdateUserDto updateUserDto) throws UserNotFoundException {
        User user = findUserById(userId);
        userMapper.updateEntity(user, updateUserDto);
    }

    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    public boolean updateUserPassword(long userId, UpdatePasswordDto updatePasswordDto) throws UserNotFoundException {
        String oldPassword = updatePasswordDto.getOldPassword();
        String newPassword = updatePasswordDto.getNewPassword();
        User user = findUserById(userId);

        if (passwordEncoder.matches(oldPassword, user.getPassword().getPasswordHash())) {
            String newPasswordHash = passwordEncoder.encode(newPassword);
            user.getPassword().setPasswordHash(newPasswordHash);
            return true;
        }
        return false;
    }

    public Optional<UserDto> login(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        var userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        String hash = user.getPassword().getPasswordHash();

        if (passwordEncoder.matches(password, hash)) {
            UserDto dto = userMapper.mapToDto(user);
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    public UserDto register(NewUserDto newUserDto) {
        UserDto userDto = addUser(newUserDto);
        User user = userRepository.findById(userDto.getUserId())
                .orElseThrow(IllegalStateException::new);

        var userRole = globalRoleRepository
                .findByName("USER")
                .orElseThrow(IllegalStateException::new);

        user.setGlobalRoles(Set.of(userRole));
        return userMapper.mapToDto(user);
    }

    private User findUserById(long userId) throws UserNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setGlobalRoleRepository(GlobalRoleRepository globalRoleRepository) {
        this.globalRoleRepository = globalRoleRepository;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
