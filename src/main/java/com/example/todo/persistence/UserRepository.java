package com.example.todo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.todo.model.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>{
	UserEntity findByEmail(String email);
	Boolean existsByEmail(String email);
	//UserEntity findByUsername(String username);
	Optional<UserEntity> findById(String id);
}