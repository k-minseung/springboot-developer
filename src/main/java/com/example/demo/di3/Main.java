package com.example.demo.di3;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.reflect.ClassPath;


//객체 자동등록하기
//스프링부트에서 어떻게 객체를 자동으로 만들어놓고 시작할 수 있는가?
//ComponentScanning
//클래스 앞에 @Component 어노테이션을 붙이고 패키지에 컴포넌트 어노테이션이 붙어있는
//클래스를 찾아서 객체로 만들어 Map형태로 저장하는 기법

//자동차는 엔진과 문이 필요하다.
@Component class Car{
	@Autowired
	Engine engine; //객체 변수 -> null
	
	@Autowired
	Door door; //객체 변수 -> null
	
	@Override
	public String toString() {
		return "Car[engine="+engine+", door="+door+"]";
	}
};
@Component class SportCar extends Car{};
@Component class Truck extends Car{};
@Component class Engine{};
@Component class Door{};

//객체 컨테이너 역할을 하는 클래스
class AppContext{
	Map map;
	
	public AppContext() {
		map = new HashMap();//객체를 저장하기 위한 map을 준비
		doComponentScan();//컴포넌트 스캐닝을 해서 map에 저장해주는 메서드
		doAutowired();
	}
	
	private void doAutowired() {
		//map에 저장된 객체의 객체변수중에 @Autowired가 붙어있으면
		//객체변수의 타입에 맞는 객체를 찾아서 연결
		try {
			for(Object bean : map.values()) {
				//Field클래스
				//클래스의 멤버에 대한 정보를 표현하고 조작할 수 있게 해준다.
				//특정 클래스에 선언된 멤버 변수를 동적으로 읽거나 수정하는 데 사용된다.
				for(Field fld : bean.getClass().getDeclaredFields()) {
					if(fld.getAnnotation(Autowired.class)!= null) {
						//필드에 Type을통해 얻어온 객체를 대입
						fld.set(bean, getBean(fld.getType()));
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
		
		
		
		
		
	}
	
	private void doComponentScan() {
		try {
			//1. 패키지내의 클래스 목록을 가져온다.
			//2. 반복문으로 클래스를 하나씩 읽어서 @Component가 붙어있는지 확인
			//3. @Component가 붙어있으면 객체를 생성해서 Map에 저장
			
			//클래스로더는 JVM에서 클래스를 동적으로 로드하고, 애플리케이션이 사용할 수 있도록
			//메모리에 적재하는 역할을 한다.
			ClassLoader classLoader = AppContext.class.getClassLoader();
			
			//ClassPath는 구아바에서 제공하는 기능으로, 클래스 경로 상의 모든 클래스를 탐색하고
			//사용할 수 있게 해준다.
			ClassPath classPath = ClassPath.from(classLoader);
			
			//지정한 패키지내의 최상위 클래스들을 가져온다.
			//이 메서드는 지정된 패키지에서 상위 레벨 클래스를 탐색하고, 그 결과로 ClassPath.ClassInfo 객체들의
			//집합(Set)을 반환한다.
			Set<ClassPath.ClassInfo> set = classPath.getTopLevelClasses("com.example.demo.di3"); 
			
			//위에서 얻는 클래스 정보를 반복으로 처리한다.
			//각 ClassPath.ClassInfo 객체는 특정 클래스에 대한 정보를 나타낸다.
			for(ClassPath.ClassInfo classInfo : set) {
				//현재의 ClassInfo객체를 실제로 로드된 클래스(Class)로 변환한다.
				//이 메서드는 해당 클래스의 정보를 기반으로 JVM에서 해당 클래스를 로드하여 Class객체를 반환한다.
				Class clazz = classInfo.load();
				
				//해당 클래스에 @Component 어노테이션이 붙어있는지 확인
				Component component = (Component)clazz.getAnnotation(Component.class);
				
				//해당 클래스가 @Component로 지정된 클래스라면
				if(component != null) {
					//클래스 이름의 첫 글자를 소문자로 변환하여 id로 사용한다.
					//클래스 이름의 첫글자를 소문자로 변환하는 이유는
					//스프링에서 빈을 생성할 때, 기본적으로 클래스 이름의 첫 글자를 소문자로 사용하기 때문이다.
					String id = StringUtils.uncapitalize(classInfo.getSimpleName());
					
					map.put(id, clazz.newInstance());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	//객체 찾기
	//by Name
	
	//만들어진 객체를 사용하기 위해서 getBean메서드를 만든다
	Object getBean(String key) {
		return map.get(key);
	}
	
	//by Type
	Object getBean(Class clazz) {
		for(Object obj : map.values()) {
			if(clazz.isInstance(obj)) {
				return obj;
			}
		}
		return null;
	}
}


public class Main {
	public static void main(String[] args) {
		AppContext ac = new AppContext();
		
		Car car = (Car)ac.getBean("car");
		
		System.out.println("car="+car);
		System.out.println("engine="+car.engine);
		System.out.println("door="+car.door);
		
	}
}
