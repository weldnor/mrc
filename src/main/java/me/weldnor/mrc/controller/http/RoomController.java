package me.weldnor.mrc.controller.http;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.dto.room.NewRoomDto;
import me.weldnor.mrc.domain.dto.room.RoomDto;
import me.weldnor.mrc.exception.room.RoomNotFoundException;
import me.weldnor.mrc.service.RoomService;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/rooms")
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

    @PutMapping
    public RoomDto addRoom(@RequestBody NewRoomDto newRoomDto) {
        return roomService.addRoom(newRoomDto);
    }

    @DeleteMapping
    public void deleteAllRooms() {
        roomService.deleteAllRooms();
    }

    @GetMapping("/{roomId}")
    public RoomDto getRoom(@PathVariable ObjectId roomId) throws RoomNotFoundException {
        return roomService.getRoom(roomId);
    }

    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable ObjectId roomId) {
        roomService.deleteRoom(roomId);
    }
}
