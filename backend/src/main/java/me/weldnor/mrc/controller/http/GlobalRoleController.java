package me.weldnor.mrc.controller.http;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.dto.globalrole.GlobalRoleDto;
import me.weldnor.mrc.dto.globalrole.NewGlobalRoleDto;
import me.weldnor.mrc.dto.globalrole.UpdateGlobalRoleDto;
import me.weldnor.mrc.exception.globalrole.GlobalRoleNotFoundException;
import me.weldnor.mrc.service.GlobalRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/global-roles")
@Slf4j
public class GlobalRoleController {

    private final GlobalRoleService globalRoleService;

    public GlobalRoleController(GlobalRoleService globalRoleService) {
        this.globalRoleService = globalRoleService;
    }

    @GetMapping
    public List<GlobalRoleDto> getAllGlobalRoles() {
        return globalRoleService.getAllGlobalRoles();
    }

    @PostMapping
    public GlobalRoleDto addGlobalRole(@RequestBody NewGlobalRoleDto newGlobalRoleDto) {
        return globalRoleService.addGlobalRole(newGlobalRoleDto);
    }

    @DeleteMapping
    public void deleteAllGlobalRoles() {
        globalRoleService.deleteAllGlobalRoles();
    }

    @GetMapping("/{globalRoleId}")
    public GlobalRoleDto getGlobalRole(@PathVariable long globalRoleId) throws GlobalRoleNotFoundException {
        return globalRoleService.getGlobalRole(globalRoleId);
    }

    @PutMapping("/{globalRoleId}")
    public void updateGlobalRole(@PathVariable long globalRoleId, @RequestBody UpdateGlobalRoleDto updateUserDto) throws GlobalRoleNotFoundException {
        globalRoleService.updateGlobalRole(globalRoleId, updateUserDto);
    }

    @DeleteMapping("/{globalRoleId}")
    public void deleteGlobalRole(@PathVariable long globalRoleId) {
        globalRoleService.deleteGlobalRole(globalRoleId);
    }
}
