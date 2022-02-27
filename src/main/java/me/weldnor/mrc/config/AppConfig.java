package me.weldnor.mrc.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;

@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(ObjectId.class, new ObjectIdJsonSerializer());
        module.addSerializer(Instant.class, new InstantSerializer());
        mapper.registerModule(module);

        return mapper;
    }

    private static class ObjectIdJsonSerializer extends JsonSerializer<ObjectId> {
        @Override
        public void serialize(ObjectId o, JsonGenerator j, SerializerProvider s) throws IOException {
            if (o == null) {
                j.writeNull();
            } else {
                j.writeString(o.toString());
            }
        }
    }

    private static class InstantSerializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant o, JsonGenerator j, SerializerProvider s) throws IOException {
            if (o == null) {
                j.writeNull();
            } else {
                j.writeString(o.toString());
            }
        }
    }

}
