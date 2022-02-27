package me.weldnor.mrc.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.weldnor.mrc.domain.entity.User;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private ObjectId id;
    private String email;
    private String name;
    private String passwordHash;
    private User.Role role;
}
