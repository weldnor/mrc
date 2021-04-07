package me.weldnor.mrc.service;


import me.weldnor.mrc.dto.globalrole.GlobalRoleDto;
import me.weldnor.mrc.dto.globalrole.NewGlobalRoleDto;
import me.weldnor.mrc.dto.globalrole.UpdateGlobalRoleDto;
import me.weldnor.mrc.exception.globalrole.GlobalRoleNotFoundException;

import java.util.List;

public interface GlobalRoleService {
    List<GlobalRoleDto> getAllGlobalRoles();

    GlobalRoleDto addGlobalRole(NewGlobalRoleDto newGlobalRoleDto);

    void deleteAllGlobalRoles();

    GlobalRoleDto getGlobalRole(long globalRoleId) throws GlobalRoleNotFoundException;

    void updateGlobalRole(long globalRoleId, UpdateGlobalRoleDto updateUserDto) throws GlobalRoleNotFoundException;

    void deleteGlobalRole(long globalRoleId);
}
