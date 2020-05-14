# Flight Reservation

# 예제 - 항공 예약 시스템

- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [예제 - 항공권예약](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)


# 서비스 시나리오

기능적 요구사항
1. 고객이 항공권 예약을 요청한다.
2. 예약이 요청되면 결제를 요청한다.
3. 항공권의 가격은 100원으로 제한한다.
4. 항공권의 가격이 고객의 잔고보다 낮으면 결제가 성공된다.
5. 항공권의 가격이 고객의 잔고보다 높으면 결제가 실패된다.
6. 고객이 예약을 취소한다.
7. 고객이 예약을 취소하면 결젝 취소된다.
8. 결제가 취소되면 고객의 잔고가 +100 된다.
9. 예약이 성공하면 티켓을 발행한다.
10. 예약이 취소되면 티켓 발행을 취소한다.
11. 예약 진행 상황을 알람을 받는다.

비기능적 요구사항
1. 트랜잭션
    1. 모든 트랜잭션은 비동기 식으로 구성한다.
2. 장애격리
    1. 결제 시스템이 수행되지 않더라도 예약은 365일 24시간 받을 수 있어야 한다. Async (event-driven), Eventual Consistency
    1. 결제 시스템이 과중되면 예약을 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다. user에게는 Pending상태로 보여준다. Circuit breaker, fallback
3. 성능
    1. 고객이 예약 상황을 예약 리스트(프론트엔드)에서 확인할 수 있어야 한다. CQRS
    1. 예약 진행 상황을 카톡 등으로 알림을 줄 수 있어야 한다. Event driven


# 분석/설계

## TO-BE 조직 (Vertically-Aligned)
![image](https://user-images.githubusercontent.com/63623995/81637644-cf55f400-9451-11ea-9cf3-1b599eb21e5d.png)


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과: http://msaez.io/#/storming/u41qKiD4gfaXC23EQCoZq4IvtuI2/mine/24b47ed1d8087379fff017f5f6671876/-M71gjp1bQwtcugFX4tl


### 이벤트 도출
원할한 토론, 이해를 위해 event-storming 초반은 한글로 작성 및 진행
![image](https://user-images.githubusercontent.com/63623995/81629770-e9d2a200-943e-11ea-8a95-02c1bae75b1b.png)

### 액터, 커맨드 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/63623995/81630226-fe636a00-943f-11ea-93d5-67f851180950.png)

### 어그리게잇으로 묶기
![image](https://user-images.githubusercontent.com/63623995/81630407-77fb5800-9440-11ea-8957-8cc538b21489.png)

    - 예약, 결제, 발행 중심으로 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 묶어줌

### 바운디드 컨텍스트로 묶기
![image](https://user-images.githubusercontent.com/63623995/81630582-fce67180-9440-11ea-9b2d-1e201e0c385a.png)

    - 도메인 서열 분리 
        - Core Domain: 예약관리(front) : 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준은 예약관리 99.999% / 결제 시스템 관리 90% 목표, 배포주기는 예약관리 1주일 1회 미만/ 고객관리 2주 1회 미만으로 함
        - Supporting Domain: 결제 : 결제 시스템 관리 90% 목표, 배포주기는 예약관리 1주일 1회 미만/ 고객관리 2주 1회 미만으로 함
	- General Domain : 티켓 발행 : 추가 편의 및 알람을 통한 고객 서비스 만족을 위하 서비이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        

### 폴리시 부착 및 컨텍스트 매핑은 MSAEZ 도구 사용하여 진행
모든 언어를 영어로 변환하여, 유비쿼터스 랭귀지로 소스코드 작성 기반 마련

### 완성된 모형

![image](https://user-images.githubusercontent.com/63623995/81639169-2b227c00-9456-11ea-8e93-3a30d4344660.png)

- 각 Aggregte Attribute
  - Reservation : userId, status, userMoney, flightId
  - payment : price, flightId, userId
  - TicketIssue : flightId


### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

1. 고객의 잔고보다 비행기표값이 싸면 예약에 성공한다.
2. 고객의 잔고보다 비행기표값이 비싸면 예약에 실패한다.
3. 고객이 항공권 예약을 취소한다.
4. 예약/결제/티켓 발행이 진행되면 예약 상태가 알람으로 보내진다.
5. 고객이 현재 예약 상황을 대쉬보드로 확인할 수 있다. (View 추가로 ok)

--> 완성된 모델은 모든 기능 요구사항을 커버함.

### 비기능 요구사항에 대한 검증

 - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
  : 모든 inter-microservice 트랜잭션이 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.
 - 장애격리
  : 결제 시스템이 수행되지 않더라도 예약은 365일 24시간 받을 수 있어야 한다. Async (event-driven), Eventual Consistency
  : 결제 시스템이 과중되면 예약을 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다. user에게는 Pending상태로 보여준다. Circuit breaker, fallback
 - 성능
  : 고객이 예약 상황을 예약 리스트(프론트엔드)에서 확인할 수 있어야 한다. CQRS
  : 예약 진행 상황을 카톡 등으로 알림을 줄 수 있어야 한다. Event driven


## 헥사고날 아키텍처 다이어그램 도출
    

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 표현
    - 서브 도메인과 바운디드 컨텍스트의 분리: 각 팀의 KPI 별로 아래와 같이 관심 구현 스토리지를 나눠가짐



# 구현
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8083 이다)

```
mvn spring-boot:run  

```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 stock 마이크로 서비스). 

```
package flightReservation;

import javax.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flightReservation.config.kafka.KafkaProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long price;
    private String flightId;
    private String userId;
    private String status;

    @PostUpdate
    public void onPostUpdate(){
        String status = this.getStatus();
        System.out.println("==============onPostUpdate ====== status : "+ status);

        if(status.equals("paySucceeded")){
            PayCompleted paycompleted = new PayCompleted();

            paycompleted.setId(this.getId());
            paycompleted.setFlightId(this.getFlightId());
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;

            try {
                json = objectMapper.writeValueAsString(paycompleted);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }


            KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();

            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());


            /*BeanUtils.copyProperties(this, revsuccessed);
            revsuccessed.publishAfterCommit();*/
       }else if(status.equals("payFail")){
            PayFailed payfail = new PayFailed();

            payfail.setId(this.getId());
            payfail.setFlightId(this.getFlightId());
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;

            try {
                json = objectMapper.writeValueAsString(payfail);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }


            KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();

            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());


            /*BeanUtils.copyProperties(this, revsuccessed);
            revsuccessed.publishAfterCommit();*/
        }

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatus(String status){this.status = status;}
    public String getStatus(){return status;}



}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package flightReservation;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PaymentRepository extends CrudRepository<Payment, Long>{

    Optional<Payment> findByflightId(String flightId);

}
```
- 적용 후 REST API 의 테스트
```
성공 케이스

//먼저 Payment를 하기 위해 항공권 정보를 입력
http POST  localhost:8082/payments flightId='Japan'              

//예약 신청
1. http POST localhost:8081/reservations flightId='Japan' userMoney=150 userId='ew' status='requested'

// 예약 서비스에서 고객의 예약 상태가 결제 완료 임을 확인, 고객으 돈이 차감됨을 확인
3. http GET localhost:8081/reservations/*
```
```

실패 케이스
//먼저 Payment를 하기 위해 항공권 정보를 입력
http POST  localhost:8082/payments flightId='China'              

//예약 신청
1. http POST localhost:8081/reservations flightId='China' userMoney=70 userId='ew' status='requested'

// 예약 서비스에서 고객의 예약 상태가 결제 실패 임을 확인(돈이 모자라서 차감이 되지 않음)
3. http GET localhost:8081/reservations/*

## 퍼시스턴스
앱프런트(app) 는 H2DB를 활용하였으며 VO 선언시 @Entity 마킹되었음, 기존의 Entity Pattern 과 Repository Pattern 적용이 가능하도록 구현하였다.

```
# Reservation.java

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String userId;
    private String status;
    private Long userMoney;
    private String flightId;
    ..


# ReservationRepository.java

public interface ReservationRepository extends CrudRepository<Reservation, Long>{
    Optional<Reservation> findByflightId(String flightId);
}
```

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


항공권 예약이 이루어진 후에 결제 시스템으로 연결되느 행위는 행위는 동기식이 아니라 비 동기식으로 처리하여 예약 처리를 위하여 항공권 블로킹 되지 않도록 처리한다.
 
- 이를 위하여 항공권 예약 기록을 남긴 후에 곧바로 예약 요청이 되었다는 도메인 이벤트를 카프카로 송출한다
 
```

@Entity
@Table(name="Reservation_table")
public class Reservation {

 ...
    @PostPersist
    public void onPostPersist(){
        Requested requested = new Requested();
        requested.setUserId(this.getUserId());
        requested.setFlightId(this.getFlightId());
        requested.setUserMoney(this.getUserMoney());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;

        try {
            json = objectMapper.writeValueAsString(requested);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }

        KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();

        outputChannel.send(MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());

    }
```
- 결제 서비스에서는 항공권 예약 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
@Autowired
    PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRequested_PayRequest(@Payload Requested requested){
        System.out.println("============================= Kafka : " + requested);

        if(requested.isMe()){
            System.out.println("=============================");
            System.out.println("requested");

            if(requested.getUserMoney()>=100){
                paymentRepository.findByflightId(requested.getFlightId())
                        .ifPresent(
                                payment -> {

                                        payment.setStatus("paySucceeded");
                                        paymentRepository.save(payment);
                                        System.out.println(": paySucceeded");
                                }
                        );
            }else{
                paymentRepository.findByflightId(requested.getFlightId())
                        .ifPresent(
                                payment -> {

                                    payment.setStatus("payFail");
                                    paymentRepository.save(payment);
                                    System.out.println(": payFail");
                                }
                        );
            }

        }
        System.out.println("=============================");
    }
```
이후, 재고 차감에 성공하고 예약이 완료되면 카톡 등으로 고객에게 카톡 등으로 알람을 보낸다. 
  
```
    @Service
    public class PolicyHandler{

        @StreamListener(KafkaProcessor.INPUT)
        public void wheneverReserved_Orderresultalarm(@Payload Reserved reserved){

            if(reserved.isMe()){
                System.out.println("##### 예약 완료 되었습니다  #####" );//+ reserved.toJson());
            }

        }
    }

```

항공권 예약 시스템은 고객관리(알람) 시스템와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 고객관리 시스템이 유지보수로 인해 잠시 내려간 상태라도 예약을 받는데 문제가 없다:

# 운영

## CI/CD 설정

각 구현체들은 각자의 Git source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며, pipeline build script 는 각 서비스 프로젝트 폴더 이하에 azure-pipelines.yml 에 포함되었다.


### 설정
- Pipeline 생성 (Reservation, Stockmanagement, Customermanagement)
![pipe](https://user-images.githubusercontent.com/63623995/81764531-c0804780-950c-11ea-9553-402bba09c0ca.png)

- Pipeline 설정(모두 동일)
![pipe2](https://user-images.githubusercontent.com/63623995/81764550-c9711900-950c-11ea-8210-030511bde916.png)

- Release 생성
![release](https://user-images.githubusercontent.com/63623995/81764553-cbd37300-950c-11ea-94f7-892dc0b68ddc.png)

- Release 설정(모두 동일)
![release2](https://user-images.githubusercontent.com/63623995/81764558-cd04a000-950c-11ea-91a4-018f0c2a3b87.png)
![release3](https://user-images.githubusercontent.com/63623995/81764561-ce35cd00-950c-11ea-9052-22c52c21e584.png)

