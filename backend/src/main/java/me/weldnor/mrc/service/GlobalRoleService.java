package me.weldnor.mrc.service;


import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.dto.globalrole.GlobalRoleDto;
import me.weldnor.mrc.dto.globalrole.NewGlobalRoleDto;
import me.weldnor.mrc.dto.globalrole.UpdateGlobalRoleDto;
import me.weldnor.mrc.entity.GlobalRole;
import me.weldnor.mrc.exception.globalrole.GlobalRoleNotFoundException;
import me.weldnor.mrc.mapper.GlobalRoleMapper;
import me.weldnor.mrc.repository.GlobalRoleRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class GlobalRoleService {
    private final GlobalRoleRepository globalRoleRepository;
    private final GlobalRoleMapper globalRoleMapper;

    public GlobalRoleService(GlobalRoleRepository globalRoleRepository, GlobalRoleMapper globalRoleMapper) {
        this.globalRoleRepository = globalRoleRepository;
        this.globalRoleMapper = globalRoleMapper;
    }


    public List<GlobalRoleDto> getAllGlobalRoles() {
        var globalRoles = new ArrayList<>(globalRoleRepository.findAll());
        return globalRoleMapper.mapToDto(globalRoles);
    }

    public GlobalRoleDto addGlobalRole(NewGlobalRoleDto newGlobalRoleDto) {
        GlobalRole globalRole = globalRoleMapper.mapToEntity(newGlobalRoleDto);
        globalRole = globalRoleRepository.save(globalRole);
        return globalRoleMapper.mapToDto(globalRole);
    }

    public void deleteAllGlobalRoles() {
        globalRoleRepository.deleteAll();
    }

    public GlobalRoleDto getGlobalRole(long globalRoleId) throws GlobalRoleNotFoundException {
        var globalRole = findGlobalRoleById(globalRoleId);
        return globalRoleMapper.mapToDto(globalRole);
    }

    public void updateGlobalRole(long globalRoleId, UpdateGlobalRoleDto updateUserDto) throws GlobalRoleNotFoundException {
        GlobalRole globalRole = findGlobalRoleById(globalRoleId);
        globalRoleMapper.updateEntity(globalRole, updateUserDto);
    }

    public void deleteGlobalRole(long globalRoleId) {
        globalRoleRepository.deleteById(globalRoleId);
    }

    private GlobalRole findGlobalRoleById(long globalRoleId) throws GlobalRoleNotFoundException {
        return globalRoleRepository.findById(globalRoleId)
                .orElseThrow(() -> new GlobalRoleNotFoundException(globalRoleId));
    }
}
