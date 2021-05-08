package me.weldnor.mrc.service;


import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.dto.globalrole.GlobalRoleDto;
import me.weldnor.mrc.domain.entity.GlobalRole;
import me.weldnor.mrc.domain.entity.User;
import me.weldnor.mrc.exception.globalrole.GlobalRoleNotFoundException;
import me.weldnor.mrc.exception.user.UserNotFoundException;
import me.weldnor.mrc.mapper.GlobalRoleMapper;
import me.weldnor.mrc.repository.GlobalRoleRepository;
import me.weldnor.mrc.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class UserGlobalRoleService {

    private final UserRepository userRepository;
    private final GlobalRoleRepository globalRoleRepository;
    private final GlobalRoleMapper globalRoleMapper;

    public UserGlobalRoleService(UserRepository userRepository, GlobalRoleRepository globalRoleRepository, GlobalRoleMapper globalRoleMapper) {
        this.userRepository = userRepository;
        this.globalRoleRepository = globalRoleRepository;
        this.globalRoleMapper = globalRoleMapper;
    }

    public List<GlobalRoleDto> getAllGlobalRoles(long userId) throws UserNotFoundException {
        User user = findUserById(userId);
        List<GlobalRole> globalRoles = new ArrayList<>(user.getGlobalRoles());
        return globalRoleMapper.mapToDto(globalRoles);
    }

    public void addGlobalRole(long userId, long globalRoleId) throws UserNotFoundException, GlobalRoleNotFoundException {
        User user = findUserById(userId);
        GlobalRole globalRole = findGlobalRoleById(globalRoleId);
        user.getGlobalRoles().add(globalRole);
    }

    public void deleteAllGlobalRoles(long userId) throws UserNotFoundException {
        User user = findUserById(userId);
        user.getGlobalRoles().clear();
    }

    public void deleteGlobalRole(long userId, long globalRoleId) throws UserNotFoundException, GlobalRoleNotFoundException {
        User user = findUserById(userId);
        GlobalRole globalRole = findGlobalRoleById(globalRoleId);
        user.getGlobalRoles().remove(globalRole);
    }

    private User findUserById(long userId) throws UserNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private GlobalRole findGlobalRoleById(long globalRoleId) throws GlobalRoleNotFoundException {
        return globalRoleRepository.findById(globalRoleId)
                .orElseThrow(() -> new GlobalRoleNotFoundException(globalRoleId));
    }
}
