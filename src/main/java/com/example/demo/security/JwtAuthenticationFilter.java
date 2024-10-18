package com.example.demo.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	
	@Autowired
	private  TokenProvider tokenProvider;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		//필터안에서 뭘 하고 싶은지 작성(토큰을 가지고 유효한지 검증)
		try {
			//---------------------------  1. 발급된 토큰 읽어오기.  -------------------------------------------------//
			//parseBererToken(request) : request에 담긴 토큰을 꺼내는 메서드
			String token = parseBearerToken(request);
			log.info("Filter is running...");
			
			//-----------------------------   2. 토큰 검사하기.  ----------------------------------------------------//
			if(token != null && !token.equalsIgnoreCase("null"))  {
				//토큰을 통해 사용자 인증을 하고 userId를 반환받는다.
				
				String userId = tokenProvider.validateAndGetUserId(token);
				log.info("Authenticated user ID : " + userId);
			
			//사용자 인증 완료후, SecurityContext에 인증 정보를 등록
			//스프링 시큐리티에서 인증된 사용자 정보를 표현하는 추상클래스
			//인증된 사용자와 그 사용자의 권한 정보(Authorities)를 담는 역할을 한다.
			AbstractAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(userId, //Id
															null, //password
															AuthorityUtils.NO_AUTHORITIES //현재 권한 정보는 제공하지 않는다.
															);
			
			//WebAuthenticationDetailsSource()
			//request로부터 인증 세부정보를 생성하는 역할을 한다.
			//.buildDetails(request)
			//request 객체에서 인증과 관련된 추가적인 정보를 추출한다.
			//사용자의 세션ID, 클라이언트의 IP주소 등의 메타데이터와 같은 추가적인 정보
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			
			//SecurityContext
			//인증된 정보를 저장한다.
			//SecurityContextHolder : 스프링 시큐리티에서 사용자의 인증 정보와 Context를 관리하는 중심 클래스
			// 애플리케이션 내에서 현재 인증된 사용자의 정보를 저장하고 제공하는 역할을 한다.
			SecurityContext securityContext = 
					SecurityContextHolder.createEmptyContext();
			
			//securityContext.setAuthentication(authentication);
			//현재 요청에 대한 인증 정보를 SecurityContext에 저장하여
			//스프링 시큐리티가 해당 사용자를 인증된 사용자로 인식하게 해주는 메서드
			securityContext.setAuthentication(authentication);
			
			//인증을 완료한 후 setContext를 사용하여 인증된 사용자정보를 저장
			SecurityContextHolder.setContext(securityContext);
			
			}//if문
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//다음 필터가 있으면 호출해라 
		filterChain.doFilter(request, response);
		
		
	}//doFilterInternal
	
//	-------------------------------------------------------------------------------------------------------//
	//request읽어와서 토큰부분만 잘라 반환해주는 메서드
	
	//HttpServletRequset request
	//클라이언트가 하는 요청은 request 객체에 담긴다.
	private String parseBearerToken(HttpServletRequest request) {
		//Http 요청 헤더를 파싱해 Berer토큰을 반환한다.
		String bearerToken = request.getHeader("Authorization");
		
		//Barer 토큰 형식일 경우 토큰값만 반환
		if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7); //토큰 부분만 짤라서 반환
		}
		return null;
	}
	
//	-------------------------------------------------------------------------------------------------------//	
	
}































