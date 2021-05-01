package me.weldnor.mrc.mapper;

import me.weldnor.mrc.dto.room.NewRoomDto;
import me.weldnor.mrc.dto.room.RoomDto;
import me.weldnor.mrc.dto.room.UpdateRoomDto;
import me.weldnor.mrc.entity.Room;
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
public interface RoomMapper {
    Room mapToEntity(NewRoomDto dto);

    void updateEntity(@MappingTarget Room entity, UpdateRoomDto dto);

    List<RoomDto> mapToDto(List<Room> entities);

    RoomDto mapToDto(Room entity);
}
