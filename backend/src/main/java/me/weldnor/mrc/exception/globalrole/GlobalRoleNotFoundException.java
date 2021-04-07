package me.weldnor.mrc.exception.globalrole;

public class GlobalRoleNotFoundException extends Exception {
    public GlobalRoleNotFoundException() {
    }

    public GlobalRoleNotFoundException(long globalRoleId) {
        super("long globalRole with id: " + globalRoleId + " not found");
    }

    public GlobalRoleNotFoundException(String message) {
        super(message);
    }
}
