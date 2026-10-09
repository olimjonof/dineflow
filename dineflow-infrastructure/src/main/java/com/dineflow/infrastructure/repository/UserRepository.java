package com.dineflow.infrastructure.repository;

import com.dineflow.application.port.UserRepositoryPort;
import com.dineflow.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryPort {

    @Override
    Optional<User> findByEmail(String email);

    @Override
    boolean existsByEmail(String email);
}
