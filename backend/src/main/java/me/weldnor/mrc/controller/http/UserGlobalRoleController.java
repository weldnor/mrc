package me.weldnor.mrc.controller.http;


import me.weldnor.mrc.dto.globalrole.GlobalRoleDto;
import me.weldnor.mrc.exception.globalrole.GlobalRoleNotFoundException;
import me.weldnor.mrc.exception.user.UserNotFoundException;
import me.weldnor.mrc.service.UserGlobalRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping(
        path = "api/v1/users/{userId}/global-roles",
        produces = "application/json"
)
public class UserGlobalRoleController {

    private final UserGlobalRoleService userGlobalRoleService;

    public UserGlobalRoleController(UserGlobalRoleService globalRoleService) {
        this.userGlobalRoleService = globalRoleService;
    }

    @GetMapping
    public List<GlobalRoleDto> getAllGlobalRoles(@PathVariable long userId) throws UserNotFoundException {
        return userGlobalRoleService.getAllGlobalRoles(userId);
    }

    @PostMapping
    public void addGlobalRole(@PathVariable long userId, @RequestBody long globalRoleId) throws UserNotFoundException, GlobalRoleNotFoundException {
        userGlobalRoleService.addGlobalRole(userId, globalRoleId);
    }

    @DeleteMapping
    public void deleteAllGlobalRoles(@PathVariable long userId) throws UserNotFoundException {
        userGlobalRoleService.deleteAllGlobalRoles(userId);
    }

    @DeleteMapping("/{globalRoleId}")
    public void deleteGlobalRole(@PathVariable long userId, @PathVariable long globalRoleId) throws UserNotFoundException, GlobalRoleNotFoundException {
        userGlobalRoleService.deleteGlobalRole(userId, globalRoleId);
    }
}
