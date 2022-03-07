package me.weldnor.mrc.repository;


import me.weldnor.mrc.domain.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> getUserByEmail(String email);
}
