package me.weldnor.mrc.exception.room;

public class RoomNotFoundException extends Exception {
    public RoomNotFoundException(long roomId) {
        super("room with id " + roomId + " not found");
    }
}
