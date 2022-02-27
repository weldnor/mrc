package me.weldnor.mrc.advice;


import me.weldnor.mrc.exception.ApiError;
import me.weldnor.mrc.exception.room.RoomNotFoundException;
import me.weldnor.mrc.exception.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {
    @ExceptionHandler({
            UserNotFoundException.class,
            RoomNotFoundException.class,
    })
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public @ResponseBody
    ApiError handleResourceNotFoundExceptions(final Exception exception) {
        return new ApiError(exception);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    ApiError handleException(final Exception exception) {
        exception.printStackTrace();
        return new ApiError(exception);
    }

}
