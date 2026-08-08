# FORTYALL:DESK — 테니스 레슨장 운영 SaaS

## 이 프로젝트가 무엇인가
소규모 테니스 레슨장의 수기 장부·카톡·엑셀 운영을 대체하는 멀티테넌트 SaaS.
핵심 모델 = **수업권(LessonPass) + 고정 스케줄(Standing Schedule)**.
과정 프로젝트: 2026-08-03 ~ 09-16, 솔로 개발(김승진), 기획 5주 + 구현 6주.

## 진실의 원천 (Source of Truth)
1. `docs/기획서_Master_v1.5.md` — **모든 기능·정책·상태의 정본.** 외부 교차 리뷰 5회를 거쳐 확정됨.
2. `docs/시나리오_전구간검증.md` — 페르소나 기반 전 구간 시나리오(S1~S12). 테스트 케이스의 원본.
3. `docs/HANDOFF.md` — 기획 세션 인수인계 요약과 현재 진행 상태.

**규칙: 코드와 기획서가 충돌하면 기획서가 이긴다. 기획을 바꾸고 싶으면 기획서를 먼저 수정하고 코드를 바꾼다.**

## 용어 규칙 (기획서 부록 용어집 준수)
- 수업권 = LessonPass (UI에선 "충전"이라 불러도 도메인 행위는 발급 ISSUE / 조정 ADJUST / 복구 RESTORE)
- 정기 등록 = RegularEnrollment — 회원별 정기 생명주기. **홀드·Renewal Grace는 여기 산다** (스케줄 아님)
- 고정 스케줄 = StandingSchedule — 시간·슬롯 규칙만 (활성/종료)
- Renewal Grace = 활성 Pass 부재(소진 or 만료) 시 슬롯 보호 유예(기본 7일)
- 확보 = 셀프 예약이 잡아둔 횟수. 고정 레슨은 확보하지 않고 출석 시 차감

## 절대 어기면 안 되는 도메인 불변식
1. 잔여는 숫자가 아니라 **PassTransaction의 합** (Ledger). 관리자 수정도 조정 트랜잭션.
2. 소진·만료 Pass는 재등록으로 재활성화 금지 — **재등록 = 항상 새 Pass 발급** (스냅샷 혼합 방지).
   단, 복구·조정·연장은 조건 충족 시(잔여>0 & 기간 내) 활성 복귀 허용. 만료-연장 시 `만료 복구 +N` 트랜잭션 동반.
3. Materializer: (schedule_id + 날짜) 유니크, 수동 수정 레슨 덮어쓰기 금지, rolling 90일 ≥ Booking Window.
4. 동시성 5대: 동시 예약(1명만)·QR 중복(1회 차감)·승인 중복(Pass 1개)·배치 중복(레슨 1개)·이동 원자성.
5. 테넌트 격리: 모든 쿼리는 center 경계 안에서. login_phone(계정)과 contact_phone(멤버십)은 별개.

## 기술 스택
Spring Boot 3 · Spring Data JPA + QueryDSL · MySQL · Spring Security + SMS OTP(비밀번호 없음) ·
Scheduler(Materializer 배치) · React PWA + 웹푸시 · 문자 API(공용 발신번호)

**확정 버전 (2026-08-08 기준)**
| 항목 | 값 |
|---|---|
| Spring Boot | **3.5.16** (Boot 4가 아님 — 수강 중인 JPA 강의·QueryDSL 예제와 맞추기 위한 의도적 선택) |
| Java | 21 (toolchain) |
| 빌드 | Gradle (`./gradlew`) |
| QueryDSL | 5.1.0 (`:jakarta` classifier 필수) |
| DB | MySQL (운영) / H2 (테스트 런타임) |

> Boot 3 → 4 업그레이드는 W6 이후 별건으로 판단한다. Boot 4는 starter 이름(`-web` → `-webmvc`)과
> QueryDSL annotation processor 호환이 달라져, 학습 중 혼선 비용이 이득보다 크다고 판단했다.

## 프로젝트 구조
- 루트 패키지: `com.buzz.fortyall_desk` — 진입점 `FortyallDeskApplication`
- 설정: `src/main/resources/application.yaml`
- 패키지 배치 규칙(도메인별 vs 레이어별)은 **아직 미결** — W1에서 첫 엔티티를 만들 때 정한다.
- ⚠️ `application.yaml`에 **datasource 설정이 아직 없다** → `bootRun`은 현재 실패한다. W1에서 MySQL 접속 정보 추가 필요.
  (테스트는 H2가 자동 구성되므로 `./gradlew build`는 통과한다.)

**자주 쓰는 명령**
```bash
./gradlew build      # 컴파일 + 테스트
./gradlew test       # 테스트만
```

## 개발 방식
- **수직 슬라이스**: 기능 하나를 엔티티→리포지토리→서비스→API→화면까지 끝내고 다음으로.
- **완성 게이트**: 코어 루프(W1~W4)가 끝나기 전 확장 기능(NTRP·홀드·승계·2인·CSV) 착수 금지.
- 커밋 메시지·코드 주석에 기획서 절 번호를 인용 (예: `feat: 충전 유예 시작 로직 (기획서 6.3)`).
- 개발자는 백엔드 입문자(JPA 학습 중) — 새 패턴 도입 시 왜 그런지 한 줄 설명을 곁들일 것.

## 현재 상태와 다음 작업
`docs/HANDOFF.md` 참조. 시작 명령: "HANDOFF.md 읽고 W1부터 시작하자"
