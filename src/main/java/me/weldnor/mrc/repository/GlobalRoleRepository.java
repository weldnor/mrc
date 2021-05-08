package me.weldnor.mrc.repository;


import me.weldnor.mrc.domain.entity.GlobalRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlobalRoleRepository extends JpaRepository<GlobalRole, Long> {
    Optional<GlobalRole> findByName(String name);
}
