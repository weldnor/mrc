package me.weldnor.mrc.mapper;

import me.weldnor.mrc.dto.globalrole.GlobalRoleDto;
import me.weldnor.mrc.dto.globalrole.NewGlobalRoleDto;
import me.weldnor.mrc.dto.globalrole.UpdateGlobalRoleDto;
import me.weldnor.mrc.entity.GlobalRole;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface GlobalRoleMapper {
    GlobalRole mapToEntity(NewGlobalRoleDto dto);

    void updateEntity(@MappingTarget GlobalRole entity, UpdateGlobalRoleDto dto);

    List<GlobalRoleDto> mapToDto(List<GlobalRole> entities);

    GlobalRoleDto mapToDto(GlobalRole entity);
}
