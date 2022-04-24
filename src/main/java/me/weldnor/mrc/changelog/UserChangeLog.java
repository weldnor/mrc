package me.weldnor.mrc.changelog;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import me.weldnor.mrc.domain.entity.User;
import me.weldnor.mrc.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@ChangeUnit(id = "user-initializer", order = "1", author = "weldnor")
public class UserChangeLog {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserChangeLog(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Execution
    public void execution() {
        User user = new User();
        user.setName("example");
        user.setEmail("example@mail.com");
        user.setRole(User.Role.USER);
        user.setPasswordHash(passwordEncoder.encode("example"));

        userRepository.save(user);
    }

    @RollbackExecution
    public void rollbackExecution() {
        userRepository.deleteAll();
    }
}
