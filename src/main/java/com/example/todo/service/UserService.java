package com.example.todo.service;

import com.example.todo.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.todo.model.UserEntity;
import com.example.todo.persistence.UserRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	public UserEntity create(final UserEntity userEntity) {
		if (userEntity == null || userEntity.getEmail() == null) {
			throw new RuntimeException("Invalid arguments");
		}
		final String email = userEntity.getEmail();
		if (userRepository.existsByEmail(email)) {
			log.warn("Email already exists {}", email);
			throw new RuntimeException("Email already exists");
		}

		return userRepository.save(userEntity);
	}

	public UserEntity getByCredentials(final String email, final String password, final PasswordEncoder encoder) {
		final UserEntity originalUser = userRepository.findByEmail(email);
		if (originalUser != null && encoder.matches(password, originalUser.getPassword())) {
			return originalUser;
		}
		return null;
	}

	//	회원 정보를 반환하는 함수
	public UserDTO getUserInfo() {
		//현재 사용자의 Authentication 객체 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 인증 객체에서 사용자 ID 가져오기
		String userId = (String) authentication.getPrincipal();

		// 사용자 ID로 사용자 정보 가져오기
		Optional<UserEntity> userOptional = userRepository.findById(userId);
		log.info("UserService");
		// Optional에서 UserEntity를 추출하여 사용자 정보 가져오기
		if (userOptional.isPresent()) {
			UserEntity user = userOptional.get();
			// 사용자 정보가 존재하는 경우 UserDTO로 매핑하여 반환
			UserDTO userInfo = new UserDTO();
			userInfo.setEmail(user.getEmail());
			userInfo.setId(user.getId());
			userInfo.setUsername(user.getUsername());
			//userInfo.setPassword(user.getPassword());
			return userInfo;
		} else {
			return null;
		}
	}

	//회원정보 업데이트
	public UserEntity updateUser(final UserEntity updatedUserEntity) {
		// 현재 인증된 사용자의 ID를 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userId = (String) authentication.getPrincipal();

		// 해당 ID에 해당하는 사용자를 찾습니다.
		Optional<UserEntity> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new RuntimeException("User not found");
		}
		// 업데이트할 사용자 정보
		UserEntity user = optionalUser.get();
		user.setEmail(updatedUserEntity.getEmail());
		user.setUsername(updatedUserEntity.getUsername());
		user.setPassword(updatedUserEntity.getPassword());

		return userRepository.save(user);
	}
}


