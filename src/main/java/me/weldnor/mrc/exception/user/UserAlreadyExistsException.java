package me.weldnor.mrc.exception.user;

public class UserAlreadyExistsException extends Throwable {
    public UserAlreadyExistsException() {
        super("user already exists");
    }
}
