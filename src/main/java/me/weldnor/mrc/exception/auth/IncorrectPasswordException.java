package me.weldnor.mrc.exception.auth;

public class IncorrectPasswordException extends Throwable {
    public IncorrectPasswordException() {
        super("password is incorrect");
    }
}
