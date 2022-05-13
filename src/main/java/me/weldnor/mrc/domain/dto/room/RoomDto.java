package me.weldnor.mrc.domain.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDto {
    private ObjectId id;
    private ObjectId creator;
    private String name;
}
