package me.weldnor.mrc.exception.user;

import org.bson.types.ObjectId;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(ObjectId userId) {
        super("user with id " + userId + " not found");
    }
}
