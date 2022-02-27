package me.weldnor.mrc.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private ObjectId id;
    private String email;
    private String name;
    private String passwordHash;
    private Role role;

    public enum Role {
        USER, ADMIN
    }
}
