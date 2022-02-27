package me.weldnor.mrc.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private String message;
    private Instant timestamp = Instant.now();

    public ApiError(Exception exception) {
        message = exception.getMessage();
    }

    public ApiError(String message) {
        this.message = message;
    }
}
