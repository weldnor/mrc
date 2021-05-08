package me.weldnor.mrc.service;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.dto.room.NewRoomDto;
import me.weldnor.mrc.domain.dto.room.RoomDto;
import me.weldnor.mrc.domain.dto.room.UpdateRoomDto;
import me.weldnor.mrc.domain.entity.Room;
import me.weldnor.mrc.exception.room.RoomNotFoundException;
import me.weldnor.mrc.mapper.RoomMapper;
import me.weldnor.mrc.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public RoomService(RoomRepository roomRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }

    public List<RoomDto> getAllRooms() {
        var rooms = new ArrayList<>(roomRepository.findAll());
        return roomMapper.mapToDto(rooms);
    }

    public RoomDto addRoom(NewRoomDto newRoomDto) {
        var room = roomMapper.mapToEntity(newRoomDto);
        room = roomRepository.save(room);
        return roomMapper.mapToDto(room);
    }

    public void deleteAllRooms() {
        roomRepository.deleteAll();
    }

    public RoomDto getRoom(long roomId) throws RoomNotFoundException {
        var room = findRoomById(roomId);
        return roomMapper.mapToDto(room);
    }

    public void updateRoom(long roomId, UpdateRoomDto updateUserDto) throws RoomNotFoundException {
        var room = findRoomById(roomId);
        roomMapper.updateEntity(room, updateUserDto);
    }

    public void deleteRoom(long roomId) {
        roomRepository.deleteById(roomId);
    }

    private Room findRoomById(long roomId) throws RoomNotFoundException {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }
}
