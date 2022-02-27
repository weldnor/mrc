package me.weldnor.mrc.config;

import io.swagger.v3.oas.models.media.Schema;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

import static org.springdoc.core.SpringDocUtils.getConfig;

@Configuration
public class SwaggerConfig {
    static {
        Schema<ObjectId> objectidSchema = new Schema<>();
        objectidSchema.description("objectId");
        objectidSchema.example(new ObjectId().toString());
        getConfig().replaceWithSchema(ObjectId.class, objectidSchema);

        Schema<Instant> instantSchema = new Schema<>();
        instantSchema.description("instant");
        instantSchema.example(Instant.now().toString());
        getConfig().replaceWithSchema(Instant.class, instantSchema);
    }
}
