package me.weldnor.mrc.service;

import me.weldnor.mrc.domain.dto.login.LoginRequestDto;
import me.weldnor.mrc.domain.dto.user.NewUserDto;
import me.weldnor.mrc.domain.dto.user.UserDto;
import me.weldnor.mrc.domain.entity.User;
import me.weldnor.mrc.exception.auth.IncorrectPasswordException;
import me.weldnor.mrc.exception.user.UserAlreadyExistsException;
import me.weldnor.mrc.exception.user.UserNotFoundException;
import me.weldnor.mrc.mapper.UserMapper;
import me.weldnor.mrc.repository.UserRepository;
import me.weldnor.mrc.security.SimpleAuthentication;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final HttpSession session;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, HttpSession session) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.session = session;
    }


    public UserDto login(LoginRequestDto dto) throws UserNotFoundException, IncorrectPasswordException {
        String email = dto.getEmail();
        String password = dto.getPassword();

        User user = userRepository.getUserByEmail(email).orElseThrow(UserNotFoundException::new);

        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            authorize(user.getId());
        } else {
            throw new IncorrectPasswordException();
        }

        return userMapper.mapToDto(user);
    }

    public UserDto register(NewUserDto dto) throws UserAlreadyExistsException {
        String email = dto.getEmail();

        if (userRepository.getUserByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        String passwordHash = passwordEncoder.encode(dto.getPassword());

        User user = userMapper.mapToEntity(dto);
        user.setPasswordHash(passwordHash);

        user = userRepository.save(user);

        authorize(user.getId());

        // session
        session.setAttribute("userId", user.getId());

        return userMapper.mapToDto(user);
    }

    public void authorize(ObjectId userId) {
        Authentication authentication = new SimpleAuthentication(userId);
        SecurityContext context = new SecurityContextImpl(authentication);
        SecurityContextHolder.setContext(context);
    }
}
