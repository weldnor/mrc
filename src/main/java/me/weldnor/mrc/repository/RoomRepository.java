package me.weldnor.mrc.repository;

import me.weldnor.mrc.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}