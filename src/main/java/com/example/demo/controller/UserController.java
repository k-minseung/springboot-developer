package com.example.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.UserEntity;
import com.example.demo.security.TokenProvider;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/auth")
public class UserController {
	
	@Autowired
	private UserService service;
	
	@Autowired
	private TokenProvider tokenprovider;
	
	private PasswordEncoder passwordEncoder= new BCryptPasswordEncoder();
	
	//회원가입 -> 데이터베이스에 데이터를 추가하는 것
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO){
		//requestBody에 포함된 UserDTO객체를 수신하여 처리한다.
		try {
			// UserDTO를 기반으로 UserEntity객체를 생성한다.
			UserEntity user = UserEntity.builder()
									.username(userDTO.getUsername())
									//사용자에게 입력받은 비밀번호 암호화
									.password(passwordEncoder.encode(userDTO.getPassword()))
									.build();
			//UserService를 이용해 새로만든 UserEntity를 데이터베이스에 저장한다.
			UserEntity registerdUser = service.create(user);
			
			//등록된 UserEntity정보를 UserDTO로 변환하여 응답에 사용한다.
			UserDTO responseUserDTO = userDTO.builder()
										.id(registerdUser.getId())
										.username(registerdUser.getUsername())
										//.password(registerdUser.getPassword())  // 비밀번호가 보여지기 싫으면 생략하면 된다.
										.build();
			
			//성공적으로 저장된 user정보를 포함한 HTTP 200 응답을 반환한다.
			return ResponseEntity.ok().body(responseUserDTO);
		} catch (Exception e) {
			// 예외 발생시 에러메세지를 포한한 ResponseDTO객체를 만들어 응답한다.
			ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();
			
			//HTTP 400상태코드를 반환하고, 에러 메세지를 Response Body에 포함시킨다.
			return ResponseEntity.badRequest().body(responseDTO);
		}
		
		
	}
	
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO){
		 //요청본문으로 전달된 UserDTO의 username과 password를 기반으로 유저를 조회
		 UserEntity user = service.getByCredentials(
                 userDTO.getUsername(), // UserDTO에서 username 값을 가져온다.
                 userDTO.getPassword(),
                 passwordEncoder); // UserDTO에서 password 값을 가져온다.
		 //사용자가 존재한다면
		 if(user != null) {
			 //토큰의 생성
			 String token = tokenprovider.create(user);
			 
			 //인증에 성공한 경우 유저 정보를 UserDTO로 변환하여 응답에 사용한다.
			 //DTO에 토큰을 실어서 보냄
			 final UserDTO responseUserDTO = UserDTO.builder()
					 							.id(user.getId())
					 							.username(user.getUsername())
					 							.token(token)
					 							.build();
			 //성공적으로 인증된 우저 정보를 포함한 HTTP 200 응답을 반환한다.
			 return ResponseEntity.ok().body(responseUserDTO);
		 } else {
			 //유저가 존재하지 않거나 인증 실패시 에러 메시지를 포한한 ResponseDTO를 반환한다.
			 ResponseDTO responseDTO = ResponseDTO.builder().error("Login failed.") //에러메시지 설정
					 										.build();//ResponseDTO 객체를 빌드
			 
			 //HTTP 400 상태코드를 반환하고, 에러 메시지를 응답 본문에 포함시킨다.
			 return ResponseEntity.badRequest().body(responseDTO);
		 }
	}
	
}













