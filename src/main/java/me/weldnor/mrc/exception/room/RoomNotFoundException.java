package me.weldnor.mrc.exception.room;

import org.bson.types.ObjectId;

public class RoomNotFoundException extends Exception {
    public RoomNotFoundException(ObjectId roomId) {
        super("room with id " + roomId + " not found");
    }
}
