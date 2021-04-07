package me.weldnor.mrc.controller.http;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.dto.globalrole.GlobalRoleDto;
import me.weldnor.mrc.exception.globalrole.GlobalRoleNotFoundException;
import me.weldnor.mrc.service.GlobalRoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping(
        path = "api/v1/global-roles",
        produces = "application/json"
)
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

    @GetMapping("/{globalRoleId}")
    public GlobalRoleDto getGlobalRole(@PathVariable long globalRoleId) throws GlobalRoleNotFoundException {
        return globalRoleService.getGlobalRole(globalRoleId);
    }
}
