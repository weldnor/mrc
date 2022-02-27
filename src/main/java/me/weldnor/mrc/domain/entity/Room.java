package me.weldnor.mrc.domain.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    private ObjectId id;
    private ObjectId creator;
}
