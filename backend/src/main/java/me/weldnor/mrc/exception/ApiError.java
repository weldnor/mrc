package me.weldnor.mrc.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private String message;
    private LocalDateTime dateTime = LocalDateTime.now();

    public ApiError(Exception exception) {
        message = exception.getMessage();
    }

    public ApiError(String message) {
        this.message = message;
    }
}
