package me.weldnor.mrc.mapper;


import me.weldnor.mrc.dto.user.NewUserDto;
import me.weldnor.mrc.dto.user.UpdateUserDto;
import me.weldnor.mrc.dto.user.UserDto;
import me.weldnor.mrc.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User mapToEntity(NewUserDto dto);

    void updateEntity(@MappingTarget User entity, UpdateUserDto dto);

    List<UserDto> mapToDto(List<User> entities);

    UserDto mapToDto(User entity);
}
