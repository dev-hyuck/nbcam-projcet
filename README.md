
# Spring Advance - 운영형 Spring Boot

---

## 📌 학습 주제

운영 환경에서 안정적으로 동작하는 **운영형 Spring Boot 애플리케이션 구성 방법**을 학습합니다.

단순히 "실행되는 서버"가 아닌,

* ✅ **운영 상태를 확인할 수 있고**
* ✅ **환경별 설정이 분리되어 있으며**
* ✅ **로그로 문제를 추적할 수 있고**
* ✅ **보안 정보는 외부에서 안전하게 관리되는**

실제 **실무 기준의 Spring Boot 구조**를 목표로 합니다.

---

## 🎯 학습 목표

* 운영 환경과 개발 환경의 차이 이해
* Spring Boot Actuator를 활용한 상태 모니터링
* 운영 로그 전략 수립
* Profile 기반 설정 분리 이해
* AWS Parameter Store를 통한 민감 정보 관리

---

## 🧩 전체 구성 흐름

```
[Client]
   ↓
[Spring Boot Application]
   ↓
────────────────────────────────
|  Profile (local / prod)     |
|  Actuator Monitoring        |
|  Logging Strategy            |
|  AWS Parameter Store         |
────────────────────────────────
```

---

# 1️⃣ 운영형 Spring 이란?

운영형 Spring이란 다음 요소를 고려한 애플리케이션을 의미합니다.

### ❌ 단순 개발용 서버

* application.properties 하나만 사용
* 비밀번호 코드 하드코딩
* 로그 콘솔 출력만 사용
* 서버 상태 확인 불가

### ✅ 운영형 Spring 서버

* 환경별 설정 분리 (Profile)
* 민감 정보 외부 관리
* 로그 파일 관리 및 레벨 분리
* 서버 상태 및 헬스 체크 가능

---

## 운영 환경에서 반드시 필요한 것

| 항목              | 이유         |
| --------------- | ---------- |
| Profile         | 환경별 설정 분리  |
| Actuator        | 서버 상태 모니터링 |
| Logging         | 장애 원인 추적   |
| External Config | 보안 강화      |

---

# 2️⃣ Spring Boot Actuator

Spring Boot Actuator는 **애플리케이션 내부 상태를 외부로 노출**하는 모듈입니다.

---

## 📦 의존성 추가

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

---

## 주요 제공 엔드포인트

| Endpoint          | 설명              |
| ----------------- | --------------- |
| /actuator/health  | 서버 헬스 체크        |
| /actuator/info    | 애플리케이션 정보       |
| /actuator/metrics | JVM / 메모리 / CPU |
| /actuator/env     | 환경 변수           |
| /actuator/beans   | Bean 목록         |

---

## 기본 설정

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info
```

> ⚠️ 운영 서버에서는 반드시 필요한 항목만 노출해야 합니다.

---

## Health Check 예시

```json
{
  "status": "UP"
}
```

이 정보는 다음에서 사용됩니다.

* AWS ALB Health Check
* Kubernetes Liveness Probe
* 무중단 배포 판단 기준

---

# 3️⃣ 운영 로그 전략

운영 서버에서 로그는 **유일한 디버깅 수단**입니다.

---

## ❌ 잘못된 로그 방식

```java
System.out.println("로그 출력");
```

* 로그 레벨 없음
* 파일 저장 불가
* 운영 서버 추적 불가

---

## ✅ 올바른 로그 방식

```java
@Slf4j
@Service
public class OrderService {

    public void order() {
        log.info("주문 시작");
        log.warn("재고 부족 가능성");
        log.error("결제 실패");
    }
}
```

---

## 로그 레벨

| 레벨    | 설명     |
| ----- | ------ |
| TRACE | 상세 디버깅 |
| DEBUG | 개발용    |
| INFO  | 운영 기본  |
| WARN  | 잠재적 문제 |
| ERROR | 장애 발생  |

---

## 운영 로그 설정 예시

```yaml
logging:
  level:
    root: info
    com.example: debug
```

---

## 로그 파일 분리

```yaml
logging:
  file:
    name: logs/app.log
```

운영 환경에서는 반드시 파일 로그를 사용합니다.

---

# 4️⃣ Profile 별 설정 분리

Profile은 **환경(Environment)** 을 의미합니다.

---

## 왜 Profile 분리가 필요한가?

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test
spring.datasource.password=1234
```

### ❌ 문제점

* 운영 배포 시 코드 수정 필요
* 비밀번호 노출
* 환경마다 다른 빌드 필요

---

## ✅ 올바른 구조

```
application.yml                # 공통
application-local.yml          # 로컬
application-prod.yml           # 운영
```

---

## 설정 예시

### application.yml

```yaml
spring:
  profiles:
    active: local
```

---

### application-local.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:test
    username: sa
    password:
```

---

### application-prod.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/app
    username: admin
    password: ${DB_PASSWORD}
```

---

## 실행 방식

```bash
java -jar app.jar --spring.profiles.active=prod
```

---

# 5️⃣ AWS Parameter Store

AWS Parameter Store는 **민감 정보를 코드 밖에서 관리**하는 서비스입니다.

---

## 사용 이유

* GitHub에 비밀번호 노출 방지
* 환경별 값 관리
* IAM 권한 기반 접근 제어

---

## Parameter Store 예시

| Key               | Value  |
| ----------------- | ------ |
| /prod/db/password | ****** |
| /prod/jwt/secret  | ****** |

---

## Spring 연동 예시

```yaml
spring:
  config:
    import: aws-parameterstore:/prod/
```

```yaml
jwt:
  secret: ${jwt/secret}
```

---

## 구조 흐름

```
AWS Parameter Store
        ↓
Spring Config Import
        ↓
application-prod.yml
        ↓
Spring Bean 주입
```

---

# ✅ 최종 정리

### 운영형 Spring Boot 핵심 요약

* Profile로 환경 분리
* Actuator로 상태 확인
* 로그 레벨 및 파일 관리
* 민감 정보는 외부 저장소 관리
* 코드 수정 없이 환경 변경 가능

---

## 🎯 실무 기준 체크리스트

* [x] application-prod.yml 존재
* [x] 비밀번호 하드코딩 제거
* [x] Actuator health check 설정
* [x] 로그 파일 출력
* [x] AWS Parameter Store 연동

---

## 📚 다음 학습 예정

* 무중단 배포 (Blue-Green / Rolling)
* Docker + Spring Boot 운영 전략
* GitHub Actions CI/CD
* AWS EC2 + ALB 배포 구조

---

✅ **이 문서는 실무 운영 환경 기준으로 작성된 Spring Boot 운영 가이드입니다.**
