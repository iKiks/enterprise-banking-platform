package com.bank.app.auth.repository;

import com.bank.app.auth.entity.Role;
import com.bank.app.common.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
