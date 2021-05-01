package me.weldnor.mrc.exception.globalrole;

public class GlobalRoleNotFoundException extends Exception {
    public GlobalRoleNotFoundException(long globalRoleId) {
        super("long globalRole with id " + globalRoleId + " not found");
    }
}
