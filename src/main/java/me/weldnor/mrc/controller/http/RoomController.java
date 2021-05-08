package me.weldnor.mrc.controller.http;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.dto.room.NewRoomDto;
import me.weldnor.mrc.dto.room.RoomDto;
import me.weldnor.mrc.dto.room.UpdateRoomDto;
import me.weldnor.mrc.exception.room.RoomNotFoundException;
import me.weldnor.mrc.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/rooms")
@Slf4j
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomDto> getAllRooms() {
        return roomService.getAllRooms();
    }

    @PostMapping
    public RoomDto addRoom(@RequestBody NewRoomDto newRoomDto) {
        return roomService.addRoom(newRoomDto);
    }

    @DeleteMapping
    public void deleteAllRooms() {
        roomService.deleteAllRooms();
    }

    @GetMapping("/{roomId}")
    public RoomDto getRoom(@PathVariable long roomId) throws RoomNotFoundException {
        return roomService.getRoom(roomId);
    }

    @PutMapping("/{roomId}")
    public void updateRoom(@PathVariable long roomId, @RequestBody UpdateRoomDto updateUserDto) throws RoomNotFoundException {
        roomService.updateRoom(roomId, updateUserDto);
    }

    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable long roomId) {
        roomService.deleteRoom(roomId);
    }
}
