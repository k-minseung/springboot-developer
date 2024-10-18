package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ResponseDTO;
import com.example.demo.dto.TodoDTO;
import com.example.demo.model.TodoEntity;
import com.example.demo.service.TodoService;

import lombok.RequiredArgsConstructor;

//@Controller // View(html,jsp,thymelef)를 반환
@RestController //이 클래스가 컨트롤러임을 명시하는 어노테이션
//@Controller + @ResponseBody -> 직렬화 해서 반환 
@RequestMapping("todo")
@RequiredArgsConstructor
public class TodoController {
   
   //TodoService 의존성 주입하기
   //@Autowired
   //private TodoService todoService;
   
   private final TodoService service;
   
   
   
   //다른 클래스에 있는 메서드를 사용하기위해 객체를 우선 만들어야 한다.
   //스프링에서는 객체를 이미 만들어놨다. 주입만 하면 된다.
   
   //Get /todo/test 로 요청을 했을때 testTodo()메서드 호출되도록 정의하기
   //testTodo에서는 TodoService의 메서드를 호출하여 결과를 ResponseDTO의 리스트에 추가한다.
   //ResponseEntity.ok().body(response)로 반환하기
   
   //아래의 메서드가 실행되기 위한 조건
   //브라우저에 주소가 호출되야 한다. localhost:9090/todo/test
   @GetMapping("/test")
   public ResponseEntity<?> testTodo(){
      String str = service.testService();//String str = "My first todo item;
      //리스트의 생성
      //인덱스를 가지고 크기에 제한이 없는 자료구조
      List<String> list = new ArrayList<>();
      //리스트에 값 추가하기 list.add(value)
      list.add(str);
      //ResponseDTO 객체 생성
      ResponseDTO<String> response = ResponseDTO.<String>builder().data(list).build();
      //ResponseEntity에 body에 response를 실어서 응답을 보낸다.
      return ResponseEntity.ok().body(response);
   }
   
   @PostMapping
   public ResponseEntity<?> createTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto){
      try {
//         String temporaryUserId = "temporary-user"; //임시 유저 id
         
         //TodoDTO객체를 TodoEntity로 변환한다.
         TodoEntity entity = TodoDTO.toEntity(dto);
         
         //id에 null이 이미 들어가있지만 명시적으로 null로 한번 더 설정한다.
         //해당 엔티티가 새로운 데이터임을 보장하게 된다.
         entity.setId(null);
         
         //임시 userId 설정.
         //지금은 인증과 인가 기능이 없으므로 임시 유저(temporary-user)만 로그인 없이 사용 가능한
         //애플리케이션인셈이다.
         entity.setUserId(userId);
         
         //서비스 레이어의 create메서드를 호출하여, TodoEntity를 데이터베이스에 저장하는 작업을 수행한다.
         //이 메서드는 저장된 TodoEntity 객체를 저장한 리스트를 반환한다.
         //entities라는 변수명으로 코드 작성해보기
         List<TodoEntity> entities = service.create(entity);
         //추가
         //------------------------------------------------------------
         //조회
         
         //자바 스트림을 이용해 반환된 엔티티리스트를 TodoDTO객체를 담은 리스트로 반환한다.
         //response를 내보낼때 TodoDTO로 내보내기로 했으니 TodoDTO
         //entities.stream().map(TodoDTO::new) : TodoEntity객체들을 TodoDTO객체들로 변환하는 과정
         //.collect(Collectors.toList()) : Stream으로 변환된 객체들을 List로 반환
         List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
         //변환된 TodoDTO객체를 담고있는 리스트를 ResponseDTO의 data필드에 대입한다.
         ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
         
         
         return ResponseEntity.ok().body(response);//responseDTO객체를 돌려줘야함
      } catch (Exception e) {
         //혹시 예외가 발생하는 경우 responseDTO객체에 리스트 대신 error 메시지를 넣어 반환한다.
         String error = e.getMessage();
         
         ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
         return ResponseEntity.badRequest().body(response);
      }
   }
   
   //조회하는 기능만 있음
   @GetMapping
   public ResponseEntity<?> retrieveTodoList(@AuthenticationPrincipal String userId){
//      String temporaryUserId = "temporary-user";
      
      //서비스레이어의 retrieve메서드를 이용해 TodoEntity가 담겨있는 리스트를 반환받아 entities에 저장한다.
      List<TodoEntity> entities= service.retrieve(userId);
      
      //자바 스트림을 이용해 반환된 리스트를 TodoDTO객체로 변환하고 리스트로 변환하여 dtos에 저장한다.
      //map(TodoDTO::new) -> .map(entity -> new TodoDTO(entity))
      List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
      
      //변환된 dtos리스트를 이용해 ResponseDTO에 담고 ResponseEntity를 이용해 응답을 반환한다.
      ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
      return ResponseEntity.ok().body(response);
   }
   
   //외부로부터 수정하려고 하는 엔티티를 요청을통해 받는다.
   @PutMapping
   public ResponseEntity<?> updateTodo(@AuthenticationPrincipal String userId,@RequestBody TodoDTO dto){
//      String temporaryUserId = "temporary-user";
      
      //dto -> Entity로 변환
      TodoEntity entity = TodoDTO.toEntity(dto);
      
      //dto에는 userId에 대한 정보가 없기 때문에 임시유저를 묶어서 보내야한다.
      entity.setUserId(userId);
      
      List<TodoEntity> entities =service.update(entity);
      
      //응답으로 돌려주려면 Entity -> DTO 변환
      //.map(TodoDTO::new) -> .map(entity -> new TodoDTO(entity))
      List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
      
      //ResonseDTO에 담아서 반환
      ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
      return ResponseEntity.ok().body(response);    
   }
   
   
   //삭제하기
   //id를 받아서 삭제
   @DeleteMapping
   public ResponseEntity<?> deleteTodo(@RequestBody TodoDTO dto,@AuthenticationPrincipal String userId){
      try {
//         String temporaryUserId = "temporary-user"; //임시 유저 아이디
         
         //1. 엔티티로 변경
         TodoEntity entity = TodoDTO.toEntity(dto);
         
         //2.임시 유저 아이디 설정.
         entity.setUserId(userId);
         
         //3. 서비스를 이용해 entity를 삭제
         List<TodoEntity> entities = service.delete(entity);
         
         //4. 자바 스트림을 이용해 반환된 엔티티 리스트드를 TodoDTO리스트로 변환한다.
         List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
         
         //5. ResponseDTO에 담아서 반환한다.
         ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
         
         return ResponseEntity.ok().body(response);
         
      } catch (Exception e) {
         // 6. 예외가 발생하는 경우 ResponseDTO에 error를 실어서 반환한다.
         String error = e.getMessage();
         ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
         return ResponseEntity.badRequest().body(response);
      }
   }   
}
