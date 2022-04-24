package me.weldnor.mrc.changelog;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import me.weldnor.mrc.domain.entity.Room;
import me.weldnor.mrc.domain.entity.User;
import me.weldnor.mrc.repository.RoomRepository;
import me.weldnor.mrc.repository.UserRepository;

@ChangeUnit(id = "room-initializer", order = "2", author = "weldnor")
public class RoomChangeLog {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public RoomChangeLog(UserRepository userRepository, RoomRepository roomRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Execution
    public void execution() {
        User exampleUser = userRepository.getUserByEmail("example@mail.com").orElseThrow();

        Room room = new Room();
        room.setCreator(exampleUser.getId());

        roomRepository.save(room);
    }

    @RollbackExecution
    public void rollbackExecution() {
        roomRepository.deleteAll();
    }
}
