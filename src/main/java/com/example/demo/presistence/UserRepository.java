package com.example.demo.presistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.stereotype.Repository;

import com.example.demo.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,String>{
	
	//username값으로UserEntity를 찾아서 반환한다.
	UserEntity findByUsername(String username);
	
	//해당 username이 존재하면 true, 아니면 false를 반환.
	Boolean existsByUsername(String username);
	
	//username과 password를 기준으로 UserEntity를 조회하는 메서드
	UserEntity findByUsernameAndPassword(String username,String password);
	//And가 사용되었음으로 두 필드를 모두 만족하는 데이터를 찾는 조건으로 쿼리가 자동 생성된다.
	
	
	
	
	
	
}

































