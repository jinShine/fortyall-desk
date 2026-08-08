# HANDOFF — 기획 세션 → 구현 세션 인수인계

> 이 문서는 5주간의 기획 채팅 세션(Claude.ai)을 Claude Code 구현 세션으로 잇는 다리다.
> 최종 갱신: 2026-08-08 (기획서 발표 전날)

## 지금까지 일어난 일 (한 문단)
테니스장 실사용 경험에서 출발해 인터뷰 방식으로 요구사항을 도출했고, 실사용 앱 2종(볼랩·ProField)을
벤치마킹했다. 핵심 모델을 "미이행·보강권·연장" 3개념에서 **수업권+고정 스케줄** 단일 구조로 단순화했고,
전 구간 시나리오 검증(S1~S12)과 외부(GPT) 교차 리뷰 5회를 거쳐 기획서 Master v1.5로 확정했다.
프로젝트명 FORTYALL:DESK (시리즈: FORTYALL:DRAW = 차기 대회 운영 앱).

## 확정된 큰 결정들 (상세는 기획서 14장 결정 이력)
- 계정(번호 1) : 멤버십(센터별 역할) = 1:N, 로그인 번호 ↔ 센터 연락처 분리
- 센터 온보딩 = 운영자 셀프 가입(무료 시작) / 회원 = 관리자 생성 + OTP 활성화(공개 가입 없음)
- 수업권: 횟수권만, 잔여/확보/사용, 출석 시 차감, 정당 결번은 안 깎임, 노쇼·늦은취소만 차감(복구 가능)
- 재등록 = 새 Pass 발급, 선결제 = 대기 상태 + 승계 시점 기산, 외상 = Carry-over 정산
- Renewal Grace(기본 7일) = 활성 Pass 부재 시 슬롯 보호 — RegularEnrollment의 상태
- 홀드 = 회원별(참가자별), 전원 홀드일 때만 슬롯 잠금·수동 개방
- 예약 변경: 이번 주만 = 즉시+알림 / 고정 변경 = 관리자 승인+재검증
- QR = 회원 제시 + 코치 스캔(짧은 TTL 서명 토큰), 상태 5종, 미처리 레슨은 관리자 큐
- Materialize rolling 90일 ≥ Booking Window(기본 30, 최대 60)
- 코트 자원·알림톡·인앱 메시지·후기·기간권 = 스코프 아웃 (기획서 11장)

## 현재 진행 상태
- [x] 기획서 Master v1.5 확정 (docs/기획서_Master_v1.5.md)
- [x] 전 구간 시나리오 12편 (docs/시나리오_전구간검증.md)
- [x] 발표 PPT 12장 (FORTYALL_DESK_기획발표.pptx — 별도 보관)
- [x] ERD 초안: 11개 엔티티 관계 확정 (PPT의 ERD ①②③ 슬라이드가 최신)
- [ ] 8/9(예정) 기획서 발표
- [x] **기획 문서 → 코드 저장소 이관 완료** (2026-08-08) — CLAUDE.md·docs/ 3종을 구현 저장소에 편입
- [~] W1 시작 — 아래 "W1 첫 작업" 1번 완료, 2~4번 진행 예정

## ERD 초안 요약 (구현 시 출발점)
핵심 축: ACCOUNT →(1:N) MEMBERSHIP ←(1:N) CENTER · CENTER→PRODUCT
MEMBERSHIP→LESSON_PASS(←PRODUCT 스냅샷)→PASS_TRANSACTION
MEMBERSHIP→REGULAR_ENROLLMENT(active/next pass, 상태: 활성·홀드·유예·종료)
REGULAR_ENROLLMENT→SCHEDULE_PARTICIPANT←STANDING_SCHEDULE→LESSON→LESSON_PARTICIPANT(pass_id 차감)
※ 개발자는 ERD 뉴비 — 엔티티 만들 때마다 관계 설계 이유를 짚어주며 진행하기로 함.

## 6주 구현 계획 (전부 완성 목표, 완성 게이트 방식)
- W1 (8/3~): 프로젝트 셋업 · ERD 확정 · 계정/OTP 로그인 · 멀티테넌트 골격
- W2: 센터 셀프 가입 · 회원 등록 · 수업권 발급(Ledger)
- W3: 고정 스케줄 · Materializer · 셀프 예약  ← **W3 종료 시 중간 점검(범위 재확인) 약속됨**
- W4: QR 출석 · 결번 · 충전 유예 · 노쇼
- W5: 회원 PWA 화면 · 알림(푸시·문자)
- W6 (~9/16): 통합 QA · 시연 데이터 · 발표 — 여유 시 확장(NTRP→홀드→승계→2인→CSV 순)

## W1 첫 작업 (여기서부터 시작)
1. ~~Spring Boot 3 프로젝트 생성 (Gradle, Java 21, JPA·QueryDSL·Security·Validation·MySQL)~~
   → **완료 (2026-08-08)**. Boot **3.5.16** / Java 21 / Gradle. `./gradlew build` 통과.
   ※ 최초 생성분은 Boot 4.0.7이었으나 강의·QueryDSL 호환을 위해 3.5.16으로 조정(→ CLAUDE.md 기술 스택 절).
   ※ 남은 것: `application.yaml`에 MySQL datasource 설정 (현재 없어서 `bootRun` 불가).
2. docs의 ERD 초안대로 Account · Center · Membership 엔티티 + 연관관계 (여기가 JPA 학습 1차 실전)
3. SMS OTP 로그인 흐름 (개발 단계에선 문자 발송 mock, 콘솔 출력)
4. 테넌트 격리 기반 규칙 확립 (center_id 스코프)

## 함께 일하는 방식 (개발자 프로필)
- 프론트엔드 개발자, 백엔드/JPA 입문 — 인프런 JPA 강의 Step 15(QueryDSL 셋업)까지 수강
- 선호: 개념 하나씩 검증하며 진행, 과설계 반대("이거 진짜 필요해?"를 잘 물음), 결정은 본인이
- 매 슬라이스 끝에 "방금 뭘 배웠는지" 한 줄 정리를 좋아함
