package me.weldnor.mrc.domain.pojo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class UserSession {
    private long userId;
    private long roomId;
    private boolean isCreator;
}

