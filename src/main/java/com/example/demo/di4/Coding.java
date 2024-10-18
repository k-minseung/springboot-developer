package com.example.demo.di4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

//코딩을 하려면 컴퓨터가 필요하다.

@Component
//final이나 @NonNull이 붙어있는 필드만 생성자의 매개변수로 생성
@RequiredArgsConstructor //생성자 생성
@Getter
public class Coding {

	//final이나 @NonNull붙히고
	private final Computer computer; //new Computer();
	

}






