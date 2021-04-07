package me.weldnor.mrc.service;


import me.weldnor.mrc.dto.globalrole.GlobalRoleDto;
import me.weldnor.mrc.exception.globalrole.GlobalRoleNotFoundException;
import me.weldnor.mrc.exception.user.UserNotFoundException;

import java.util.List;

public interface UserGlobalRoleService {
    List<GlobalRoleDto> getAllGlobalRoles(long userId) throws UserNotFoundException;

    void addGlobalRole(long userId, long globalRoleId) throws UserNotFoundException, GlobalRoleNotFoundException;

    void deleteAllGlobalRoles(long userId) throws UserNotFoundException;

    void deleteGlobalRole(long userId, long globalRoleId) throws UserNotFoundException, GlobalRoleNotFoundException;
}
