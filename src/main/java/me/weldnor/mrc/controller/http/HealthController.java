package me.weldnor.mrc.controller.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> getHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        return status;
    }
}
