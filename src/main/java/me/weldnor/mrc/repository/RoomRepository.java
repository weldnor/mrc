package me.weldnor.mrc.repository;

import me.weldnor.mrc.domain.entity.Room;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoomRepository extends MongoRepository<Room, ObjectId> {
}
