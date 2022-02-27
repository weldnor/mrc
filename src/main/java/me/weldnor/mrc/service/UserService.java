package me.weldnor.mrc.service;


import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.dto.user.NewUserDto;
import me.weldnor.mrc.domain.dto.user.UserDto;
import me.weldnor.mrc.domain.entity.User;
import me.weldnor.mrc.exception.user.UserNotFoundException;
import me.weldnor.mrc.mapper.UserMapper;
import me.weldnor.mrc.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> getAllUsers() {
        var users = userRepository.findAll();
        return userMapper.mapToDto(users);
    }

    public UserDto addUser(NewUserDto dto) {
        User user = userMapper.mapToEntity(dto);

        String passwordHash = passwordEncoder.encode(dto.getPassword());

        user = userRepository.save(user);
        return userMapper.mapToDto(user);
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    public UserDto getUser(ObjectId id) throws UserNotFoundException {
        User user = findUserById(id);
        return userMapper.mapToDto(user);
    }

    public void deleteUser(ObjectId id) {
        userRepository.deleteById(id);
    }

//    public Optional<UserDto> login(LoginRequestDto loginRequestDto) {
//        String email = loginRequestDto.getEmail();
//        String password = loginRequestDto.getPassword();
//
//        var userOptional = userRepository.findByEmail(email);
//
//        if (userOptional.isEmpty()) {
//            return Optional.empty();
//        }
//
//        User user = userOptional.get();
//
//        String hash = user.getPassword();
//
//        if (passwordEncoder.matches(password, hash)) {
//            UserDto dto = userMapper.mapToDto(user);
//            return Optional.of(dto);
//        }
//        return Optional.empty();
//    }
//
//    public UserDto register(NewUserDto newUserDto) {
//        UserDto userDto = addUser(newUserDto);
//        User user = userRepository.findById(userDto.getUserId())
//                .orElseThrow(IllegalStateException::new);
//
//        var userRole = globalRoleRepository
//                .findByName("USER")
//                .orElseThrow(IllegalStateException::new);
//
//        user.setGlobalRoles(Set.of(userRole));
//        return userMapper.mapToDto(user);
//    }

    private User findUserById(ObjectId id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
