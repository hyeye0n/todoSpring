package com.example.todo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.todo.dto.ResponseDTO;
import com.example.todo.dto.UserDTO;
import com.example.todo.model.UserEntity;
import com.example.todo.security.TokenProvider;
import com.example.todo.service.UserService;

import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {
	@Autowired
	private UserService userService;

	@Autowired
	private TokenProvider tokenProvider;

	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
		try {
			UserEntity user = UserEntity.builder()
					.email(userDTO.getEmail())
					.username(userDTO.getUsername())
					.password(passwordEncoder.encode(userDTO.getPassword()))
					.build();

			UserEntity registeredUser = userService.create(user);
			UserDTO responseUserDTO = userDTO.builder()
					.email(registeredUser.getEmail())
					.id(registeredUser.getId())
					.username(registeredUser.getUsername())
					.build();
			return ResponseEntity.ok().body(responseUserDTO);
		} catch (Exception e) {
			ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();
			return ResponseEntity.badRequest().body(responseDTO);
		}
	}

	@PostMapping("/signin")
	public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) {
		UserEntity user = userService.getByCredentials(userDTO.getEmail(), userDTO.getPassword(), passwordEncoder);

		if (user != null) {
			final String token = tokenProvider.create(user);
			final UserDTO responseUserDTO = UserDTO.builder()
					.email(user.getEmail())
					.id(user.getId())
					.token(token)
					.build();

			return ResponseEntity.ok().body(responseUserDTO);
		} else {
			ResponseDTO responseDTO = ResponseDTO.builder()
					.error("Login failed")
					.build();
			return ResponseEntity.badRequest().body(responseDTO);
		}
	}


	@GetMapping("/userinfo")
	public ResponseEntity<?> getUserInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userId = (String) authentication.getPrincipal(); // 사용자 ID 가져오기
		UserDTO userInfo = userService.getUserInfo(); // 사용자 ID를 인자로 넘겨 사용자 정보 가져오기
		if (userInfo != null) {
			return ResponseEntity.ok().body(userInfo);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
	}

	@PutMapping("/updateinfo")
	public ResponseEntity<?> updateUser(@RequestBody UserDTO updatedUserInfo) {
		try {
			// 현재 인증된 사용자의 ID를 가져오기.
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String userId = (String) authentication.getPrincipal();
			//log.info("PUT updateinfo");
			// 업데이트할 사용자 정보 생성
			UserEntity updatedUserEntity = new UserEntity();
			updatedUserEntity.setId(userId);
			updatedUserEntity.setEmail(updatedUserInfo.getEmail());
			updatedUserEntity.setUsername(updatedUserInfo.getUsername());

			// 새로운 비밀번호를 암호화하여 저장
			String encryptedPassword = passwordEncoder.encode(updatedUserInfo.getPassword());
			updatedUserEntity.setPassword(encryptedPassword);

			// 사용자 정보 업데이트
			UserEntity updatedUser = userService.updateUser(updatedUserEntity);
			// 업데이트된 사용자 정보를 UserDTO로 변환하여 반환
			if (updatedUser != null) {
				UserDTO updatedUserDTO = new UserDTO();
				updatedUserDTO.setId(updatedUser.getId());
				updatedUserDTO.setEmail(updatedUser.getEmail());
				updatedUserDTO.setUsername(updatedUser.getUsername());
				updatedUserDTO.setPassword(updatedUser.getPassword());
				return ResponseEntity.ok().body(updatedUserDTO);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
			}
		} catch (Exception e) {
			ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();
			return ResponseEntity.badRequest().body(responseDTO);
		}
	}
}