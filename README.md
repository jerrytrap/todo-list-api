# Todo-List-API

- roadmap.sh 사이트의 [[과제]](https://roadmap.sh/projects/todo-list-api) 요구사항을 구현 및 확장한 프로젝트입니다.
- 과제의 기능 요구사항을 구현한 뒤, 실제 서비스의 배포 및 운영 환경을 경험하고자 AWS EC2에 배포하고 CI/CD 환경을 구축했습니다.

<details>
<summary>요구사항 요약</summary>

## 필수 요구사항
- 회원가입 및 로그인(토큰 생성)
- Todo CRUD 기능
- 인증된 사용자만 Todo 목록 접근 가능
- 데이터 유효성 검사 및 에러 처리
- 목록 조회 시 페이지네이션 및 필터링 구현
- 데이터베이스는 자유롭게 선택

## 추가 요구사항
- 목록 조회 시 필터링 및 정렬 구현
- 단위 테스트 구현
- 요청 횟수 제한(Rate Limiting), 스로틀링 구현
- Refresh Token 구현

</details>

## 목차
1. [주요 내용](#주요-내용)
2. [기술 스택](#기술-스택)
3. [시스템 구성도](#시스템-구성도)
4. [CI/CD 구성도](#cicd-구성도)
5. [트러블슈팅](#트러블슈팅)
   - [Redis 기반 Rate Limiting 구현](#1-redis-기반-rate-limiting-구현)
   - [GitHub Actions 기반 자동 배포 구축](#2-github-actions-기반-자동-배포-구축)
   - [배포 직후 초기 응답 지연 개선](#3-배포-직후-초기-응답-지연-개선)

## 주요 내용
### 기능
- 인증: 회원가입, 로그인, 로그아웃, 토큰 재발급
  - Access token(JWT)은 응답 본문으로, Refresh token은 HttpOnly 쿠키로 전달
  - Refresh token은 Redis에 저장하고, 재발급 시 기존 토큰을 폐기한 뒤 새 토큰을 저장
- Todo 관리: 생성, 조회, 수정, 삭제
  - 페이지네이션(오프셋 기반), 정렬(생성일/제목), 필터링(준비/진행 중/완료)
- 자세한 API 명세는 [[Wiki]](https://github.com/jerrytrap/todo-list-api/wiki)에 정리해두었습니다.

### Rate Limiting
- Redis 고정 윈도우 방식, 전체 API에 동일하게 적용

### 배포 및 CI/CD
- 환경별 설정 분리: Spring Profile을 활용해 개발 및 운영 환경의 데이터베이스, Redis 등 설정을 분리하고 환경에 맞는 프로파일 적용
- Docker Compose: Spring Boot, MySQL, Redis, Caddy 컨테이너 구성
- GitHub Actions: 애플리케이션 빌드 및 Docker 이미지 생성
- AWS SSM: EC2에 배포 명령 전달 및 실행 자동화
- HTTPS: Caddy를 이용한 HTTPS 적용

### 모니터링 및 로깅
- 요청 ID 기반 로그 추적
- JSON 형식의 구조화된 로그
- Amazon CloudWatch를 통한 로그 수집

## 기술 스택

| 분류        | 사용 기술                                               |
|-----------|-----------------------------------------------------|
| 백엔드       | Java, Spring Boot, Spring Security, Spring Data JPA |
| 데이터       | MySQL, Redis                                        |
| 테스트       | JUnit 5                                             |
| 인프라       | Amazon EC2, Docker, Docker Compose, Caddy, Duck DNS |
| CI/CD     | GitHub Actions, GHCR                                |
| 로깅        | Amazon CloudWatch                                   |
| [프론트엔드](https://github.com/jerrytrap/todo-app) | JavaScript, React, Vite, Vercel                     |

## 시스템 구성도
<img width="1242" height="672" alt="아키텍처" src="https://github.com/user-attachments/assets/35bd7794-761b-4d8f-864c-e941cc7d5591" />

## CI/CD 구성도
<img width="922" height="422" alt="cicd" src="https://github.com/user-attachments/assets/37e30c46-8afe-4608-bed9-cdc642bbe90a" />

## 트러블슈팅

### 1. Redis 기반 Rate Limiting 구현
- **고정 윈도우를 선택한 이유**
  - 구현이 단순함: 키(`사용자 또는 IP + 윈도우 번호`)에 `INCR`을 하고 첫 요청에서만 `EXPIRE`를 설정
  - Redis 명령이 가볍고 키당 카운터 하나만 저장해 메모리 부담이 작음
  - 슬라이딩 윈도우, 토큰 버킷은 정확도가 높지만 구현과 저장 구조가 복잡해, 이 규모의 요구사항에는 과하다고 판단
- **한계**
  - 윈도우 경계에서 한도의 최대 2배까지 요청이 통과할 수 있음
  - `INCR`과 `EXPIRE`가 별개 명령이라 두 명령 사이의 장애 시 키가 만료 없이 남을 수 있음
  - 모든 API에 같은 한도가 적용되어 있음
- **개선 방향**
  - 슬라이딩 윈도우 카운터나 토큰 버킷(Bucket4j 등)
  - Lua 스크립트로 원자적 처리
  - API별 한도 분리 (로그인 등 민감한 API는 더 엄격하게 제한)

### 2. GitHub Actions 기반 자동 배포 구축
- **문제**: Actions는 도커 이미지 빌드까지만 하고, 서버에 접속해 `docker compose pull`, `up -d`를 직접 실행해야 해서 번거로움
- **방식 비교**
  - SSH 배포: GitHub 러너 IP가 고정되지 않아 22번 포트를 열어야 하는 보안 부담
  - SSM Run Command: 서버가 AWS로 먼저 연결해 명령을 받아가므로 **인바운드 포트가 필요 없음**
- **선택**: SSM Run Command + GitHub OIDC
  - 장기 액세스 키 없이 OIDC로 받은 임시 자격 증명 사용
  - 신뢰 정책으로 내 저장소의 `main` 브랜치만 역할을 맡을 수 있게 제한
  - 권한은 특정 인스턴스에 `SendCommand`와 결과 조회로 제한 (최소 권한)
  - 배포 후 명령 결과(`Success`/`Failed`)를 확인해 실패 시 워크플로도 실패 처리
- **한계**
  - 기존 서버를 멈추고 새로 띄우는 방식이어서, 배포하는 동안 짧은 중단이 있음
- **개선 방향**
  - 기존에 처리 중이던 요청은 끝내고 서버가 내려가도록 설정
  - 인스턴스 사양을 확장할 수 있는 경우, 블루/그린 배포 고려

### 3. 배포 직후 초기 응답 지연 개선
- **문제**: 배포 직후 첫 `/refresh`요청이 약 1566ms, `GET /todos`요청이 약 635ms로 느려지는 현상 발생
- **원인 분석**: 요청 ID(MDC)와 구간별 시간 로그로 한 요청을 분해
  - 컨트롤러에 도달하기 전 Rate Limit 필터의 첫 Redis 호출이 약 889ms로 가장 큼 (첫 연결 비용으로 해석)
  - 첫 JWT 서명 약 241ms, 첫 DB 조회(Hibernate) 약 293ms   
    --> JVM 앱이 **처음 쓰는 기능을 초기화하는 비용**이라고 판단
- **해결 방법 비교**
  - Redis, JWT, Repository를 각각 호출: 첫 `/refresh`요청 약 274ms, `GET /todos`요청 약 183ms
  - 서버 시작 후 `GET /todos` API를 직접 한 번 호출: 첫 `/refresh`요청 약 91ms, `GET /todos`요청 약 69ms   
    --> 첫 요청 응답 시간이 더 짧은 **API 직접 호출 방식**을 선택 (필터 및 직렬화 경로까지 한 번에 초기화)
- **결과**: 배포 후 첫 요청 응답 시간 2201ms → 160ms (약 92.7% 개선)
- **한계**
  - 웜업이 끝나기 전에 들어온 요청은 보호하지 못함 (웹 서버는 이미 요청을 받는 상태)
  - 서버 시작 시간이 웜업 시간만큼 늘어남 (약 1663ms)
- **개선 방향**
  - Readiness 상태를 웜업 완료 후 전환하여 초기화가 끝나기 전에 트래픽이 유입되는 문제 방지
  - 웜업 실패 시 재시도 및 시작 실패 처리 등 예외 상황 관리

\+ 각 실행 시간은 3회 측정한 후 평균값으로 산출했습니다.
