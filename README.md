### 모꿀모꿀 (MoggulMoggul) - 백엔드 서비스
![header](https://capsule-render.vercel.app/api?type=cylinder&color=EAC149&height=130&section=header&text=MOGGUL-MOGGUL%20BACKEND&fontSize=55&animation=scaleIn&fontColor=FFF)

<p align="center">
<!-- Backend & Language -->
   <img src="https://img.shields.io/badge/java-%23007396.svg?&style=for-the-badge&logo=java&logoColor=white"/>
   <img src="https://img.shields.io/badge/spring boot-%236DB33F.svg?&style=for-the-badge&logo=springboot&logoColor=white"/>
   <img src="https://img.shields.io/badge/spring security-%236DB33F.svg?&style=for-the-badge&logo=springsecurity&logoColor=white"/>
   <img src="https://img.shields.io/badge/JWT-white?style=for-the-badge&logo=jsonwebtokens&logoColor=black"/>
   <img src="https://img.shields.io/badge/gradle-%2302303A.svg?&style=for-the-badge&logo=gradle&logoColor=white"/>
   <br>
   <!-- Database & Cloud -->
   <img src="https://img.shields.io/badge/postgresql-%234169E1.svg?&style=for-the-badge&logo=postgresql&logoColor=white"/>
   <img src="https://img.shields.io/badge/redis-%23DC382D.svg?&style=for-the-badge&logo=redis&logoColor=white"/>
   <br>
   <img src="https://img.shields.io/badge/amazon s3-%23569A31.svg?&style=for-the-badge&logo=amazons3&logoColor=white"/>
   <!-- AWS Infrastructure -->
   <img src="https://img.shields.io/badge/Amazon EC2-%23FF9900.svg?&style=for-the-badge&logo=amazon-ec2&logoColor=white"/>
   <img src="https://img.shields.io/badge/Amazon RDS-%23527FFF.svg?&style=for-the-badge&logo=amazon-rds&logoColor=white"/>
   <img src="https://img.shields.io/badge/Amazon ElastiCache-%23FF9900.svg?&style=for-the-badge&logo=amazon-elasticache&logoColor=white"/>
   <br>
   <!-- Tools -->
   <img src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white"/>
   <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=Notion&logoColor=white"/>
   <img src="https://img.shields.io/badge/postman-%23FF6C37.svg?&style=for-the-badge&logo=postman&logoColor=white"/>
</p>

----

----
## 프로젝트 개요
API 제공, AI 연동 및 꿀팁 생성, 소셜 기능 처리, 핵심 데이터 관리, 사용자 인증 및 권한 관리, 실시간 알림 기능을 담당하는 백엔드 서비스 입니다.

## 팀원 소개
| **이름** | **역할**        | **GitHub**                   |
|--------|---------------|------------------------------|
| 구강현    | 백엔드 / 배포 / AI | https://github.com/GangHyoun |
| 송보민    | 백엔드 / AI      | https://github.com/Songbomin |
| 정혜지    | 프론트엔드 / 디자인   | https://github.com/heartggs  |

----
## 프로젝트 구성

### 주요 기능
- **사용자 인증**: 회원가입, 로그인, 로그아웃, 토큰 재발급 기능 (`src/main/java/com/momo/momo_backend/controller/AuthController.java`)
- **꿀팁 (Tip)**: URL을 통해 AI가 요약한 꿀팁을 생성, 저장, 수정, 삭제 및 조회 (`src/main/java/com/momo/momo_backend/controller/TipController.java`)
- **보관함 (Storage)**: 생성된 꿀팁을 개인 또는 그룹 보관함에 저장하고 관리 (`src/main/java/com/momo/momo_backend/controller/StorageController.java`)
- **소셜 기능**: 사용자 간 팔로우/언팔로우 및 관련 알림 기능 (`src/main/java/com/momo/momo_backend/controller/FollowController.java`)
- **그룹**: 그룹을 생성하고 멤버를 초대하여 꿀팁을 공유 (`src/main/java/com/momo/momo_backend/controller/GroupController.java`)
- **북마크**: 마음에 드는 꿀팁을 북마크하고 주간 랭킹 조회 (`src/main/java/com/momo/momo_backend/controller/BookmarkController.java`)
- **실시간 기능**: 새로운 꿀팁, 알림 등을 실시간으로 사용자에게 전달 (`src/main/java/com/momo/momo_backend/realtime/service/TipViewsRankService.java`)

## 툴체인 & 프레임워크

### 프레임워크

| 분류 | 사용 기술 | 설명 |
|---|---|---|
| **백엔드 프레임워크** | Spring Boot | REST API, 보안, DB 연동을 스타터 의존성 기반으로 통합 구성 |
| **보안** | Spring Security | 필터 체인 기반 인증/인가 흐름 구현, 세션리스 구조로 인증 처리 |
| **인증** | JWT (JSON Web Token) | 클라이언트 인증 상태를 유지하기 위한 토큰 기반 인증 방식 |
| **데이터베이스** | PostgreSQL | 관계형 데이터의 정합성과 트랜잭션 처리에 특화된 구조 |
| **데이터베이스** | Redis | 인메모리 기반으로 고속 키-값 저장, 실시간 랭킹 및 캐싱에 활용 |
| **데이터 액세스** | Spring Data JPA (Hibernate) | 자바 객체로 DB를 직접 제어하고 유지보수성을 높이는 ORM 접근 |
| **실시간 통신** | Spring WebSocket (STOMP) | STOMP 프로토콜을 사용하여 실시간 알림 및 데이터 전송 구현 |
| **클라우드 서비스** | AWS S3 | 프로필 이미지 등 정적 파일을 저장하고 관리 |

### 툴체인

| 분류 | 사용 기술 | 설명 |
|---|---|---|
| **IDE** | IntelliJ IDEA | Java 및 Spring Boot 개발에 최적화된 통합 개발 환경 |
| **빌드 도구** | Gradle | 프로젝트 빌드 및 의존성 관리 자동화 도구 |
| **버전 관리** | Git + GitHub | 소스 코드 이력 관리 및 협업을 위한 플랫폼 |
| **테스트 도구** | Postman, Swagger UI | REST API 테스트 및 명세 확인 |
| **기타 라이브러리** | Lombok | Getter, Setter, Builder 등 보일러플레이트 코드를 자동 생성 |
| **JDK(JRE)** | Java 17 | Spring 애플리케이션 실행을 위한 런타임 환경 |
| **DB 툴** | DBeaver | PostgreSQL 데이터베이스 접속 및 쿼리 테스트 |
| **인프라 관리** | AWS Console | EC2, RDS, S3 등 클라우드 인프라 구성 및 모니터링 |

---

## 프로젝트 프로그램 설치방법

### 사전 요구사항
- JDK 17 설치
- PostgreSQL 데이터베이스 설치 및 실행
- Redis 서버 실행
- AWS S3 버킷 및 IAM 자격 증명
- AI 서버 실행 (꿀팁 요약 기능 연동 시)

### 설치 과정

1. **프로젝트 클론**
   ```bash
   git clone [https://github.com/your-username/moggulmoggul-backend.git](https://github.com/your-username/moggulmoggul-backend.git)
   cd moggulmoggul-backend

2. **application.properties 설정**
   - src/main/resources/application.properties 파일을 생성하고 CI/CD 워크플로우 파일(.github/workflows/backend-deploy.yml)을 참고하여 아래 내용을 자신의 환경에 맞게 수정합니다.

   #### 데이터베이스 설정
   ```
   spring.datasource.url=jdbc:postgresql://<RDS_HOSTNAME>:<RDS_PORT>/<DB_NAME>
   spring.datasource.username=<RDS_USERNAME>
   spring.datasource.password=<RDS_PASSWORD>
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

   #### JWT 설정
   ```
   jwt.secret=<YOUR_JWT_SECRET_KEY>
   ```

   #### Redis 설정
   ```
   spring.data.redis.host=<REDIS_HOST>
   spring.data.redis.port=<REDIS_PORT>
   ```

   #### AWS S3 설정
      ```
      cloud.aws.s3.bucket=<S3_BUCKET_NAME>
   cloud.aws.credentials.access-key=<AWS_ACCESS_KEY>
   cloud.aws.credentials.secret-key=<AWS_SECRET_KEY>
   cloud.aws.region.static=ap-northeast-2
   cloud.aws.stack.auto=false
      ```

   #### AI 서버 URL
      ```
      ai.server.url=http://<AI_SERVER_HOST>:8000
      ```

   #### CORS 설정
      ```
      cors.allowed-origins=http://localhost:3000,[http://your-frontend-domain.com](http://your-frontend-domain.com)
      ```
   #### 실시간(WebSocket) 설정
      ```
      realtime.endpoint=/ws
   realtime.app-prefix=/app
   realtime.user-destination-prefix=/user
   realtime.cors.allowed-origins[0]=<ALLOWED_FRONTEND_URL_FOR_WEBSOCKET>
      ```

3. **프로젝트 빌드**
   <br/>
   프로젝트 루트 디렉토리에서 아래 명령어를 실행하여 프로젝트를 빌드합니다.
      ```bash
      ./gradlew build
      ```

4. **애플리케이션 실행**
   <br/>
   빌드가 완료되면 아래 명령어로 애플리케이션을 실행합니다.
    ```bash
    java -jar build/libs/moggulmoggul-backend-0.0.1-SNAPSHOT.jar
    ```
   - 애플리케이션이 성공적으로 실행되면 기본적으로 `http://localhost:8080`에서 접근할 수 있습니다.

---

## API 엔드포인트
- 인증: `/api/auth` (회원가입, 로그인, 로그아웃 등)
- 꿀팁: `/api/tips`, `/api/query/tips`, `/api/search/tips` (생성, 조회, 검색 등)
- 보관함: `/api/storage`, `/api/query/storage` (생성, 수정, 삭제, 조회)
- 그룹: `/api/groups` (생성, 멤버 초대, 조회 등)
- 팔로우: `/api/follow` (팔로우, 언팔로우, 목록 조회)
- 북마크: `/api/bookmark` (추가, 삭제, 랭킹 조회)
- 프로필: `/api/profile` (조회, 수정)
  <br/>
  더 자세한 API 명세는 [API 명세서 링크](https://www.notion.so/API-2648aa8cd0958140a4e4f5d59a636846?source=copy_link)를 참고하세요.

---

## AI 파이프라인 핵심 흐름

**생성 요청 (클→백)**
```http
POST /api/tips/generate
Content-Type: application/json

{ "url": "https://example.com/post/123" }
```

**비동기 작업 (백→AI)**
```http
POST {AI_SERVER}/async-index
```
```json
{ "task_id": "abcd-efgh-1234" }
```

**폴링 (백→AI) — 예: 최대 30회/2초 간격**
```http
GET {AI_SERVER}/task-status/{task_id}
```
**완료**
```json
{ "status": "done", "result": { "title": "…", "summary": "…", "tags": ["…"], "thumbnail": "…" } }
```
**대기**
```json
{ "status": "pending" }
```

**최종 등록 (클→백)**
```http
POST /api/tips/register
```
- 반영: `title/summary/tags/thumbnail`
- 후속: 알림 전송 · 피드 발행

**등록 규칙**
- 태그: 중복 제거 후 없으면 생성(연결: TipTag)
- 보관함: 없으면 생성/연결(StorageTip)
- (선택) URL 중복 방지/머지 정책 운영

---

## 프로젝트 URL
- **Main** : https://github.com/MoGGul-MoGGul
- **FrontEnd** : https://github.com/MoGGul-MoGGul/Frontend
- **BackEnd** : https://github.com/MoGGul-MoGGul/Backend
- **AI** : https://github.com/MoGGul-MoGGul/AI