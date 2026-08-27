# 진도 기록

> 형식: `날짜 | 한 것 | 배운 것 한 줄 | 막힌 것`
> 세션이 끊겨도 여기만 보면 이어갈 수 있게 매번 남긴다.

| 날짜 | 한 것 | 배운 것 | 막힌 것 |
|---|---|---|---|
| 2026-08-08 | 기획 문서 저장소 편입, Boot 3.5.16 스택 확정, 6주 로드맵 수립 | 스택 버전은 "최신"이 아니라 "학습 자료와 맞는 것"으로 고르는 게 나을 때가 있다 | - |
| 2026-08-09 | ERD 1교시 — 엑셀 한 장에서 테이블 8개 도출 | **값이 하나가 아니면 컬럼이 아니라 테이블이다** (= 제1정규형). 오늘 이 규칙 하나로 영업시간·NTRP·역할·잔여를 전부 풀었다 | 문서에서 엔티티 추출은 아직 어려움 → 구체적 예시에서 쪼개는 방식으로 진행 중 |
| 2026-08-09 | ERD 2·3교시 — 관계 설계 + 남은 2개 도출, **11개 완성** | **홀드는 스케줄의 사실도 참가의 사실도 아니다.** 사실이 어느 단위에 속하는지가 테이블 경계를 정한다 | 면접 Q3(잔여를 컬럼으로 안 두는 이유)는 스스로 설명 못 함 → 역할극으로 재학습 |
| 2026-08-22 | **D01** 로컬 개발 환경 — Docker MySQL + datasource 연결 | (직접 작성) | (직접 작성) |
| 2026-08-22 | **D02 / Task 2** BaseEntity·Center 엔티티, 테스트 H2 분리 | (직접 작성) | (직접 작성) |
| 2026-08-24 | **기획 재수립** — 장면 인터뷰 17개로 기획서 v2 작성, 네이밍 규칙 확정 | 설명할 수 없는 도메인은 구현해도 못 쓴다 | - |
| 2026-08-27 | **Task 3** 계정·소속·역할 — 연관관계, 유니크 제약, N+1 | **`@ManyToOne`이 있는 쪽에 FK가 생긴다.** 자바는 객체, DB는 숫자 — 그 번역이 JPA다 | (직접 작성) |

## 1교시에서 도출한 테이블 (8개)

```
member              회원 (코치도 여기 — 별도 테이블 X, 역할로 구분)
member_role         역할 (ADMIN/COACH/MEMBER, 복수 보유 가능)
product             상품 카탈로그
lesson_pass         수업권 (한 회원이 여러 장 보유)
lesson_pass_ledger    수업권 변동 원장 ← 잔여는 저장하지 않고 SUM으로 계산
recurring_schedule   고정 스케줄 규칙 (매주 목 10:00)
lesson              실제 발생한 레슨 1건
lesson_participant  레슨 참가자 + 출석 상태 (2인 레슨 대응)
```

## 2교시 — 관계 설계 완료 (2026-08-09)

**배운 규칙**
- 관계는 선이 아니라 **FK 컬럼**이다
- **FK는 항상 N쪽에 붙는다** (1쪽에 붙이면 한 칸에 값이 여러 개 = 불가능)
- 방향 판별: **"몇 개?"를 양쪽에서 두 번** 묻는다
- 양쪽 다 여러 개(N:M)면 FK로 표현 불가 → **중간 테이블**을 만든다
- **중간 테이블과 그 양쪽을 비교하면 항상 1:N** — N:M이 보이면 중간 테이블이 아직 없다는 뜻
- FK 이름 `xxx_id` = "xxx 테이블을 가리킨다". 표기는 `테이블명.컬럼명`

**확정된 관계**
```
account ──1:N──> membership <──N:1── center      (N:M → membership이 중간 테이블)
membership ──1:N──> membership_role
center ──1:N──> product ──1:N──> lesson_pass
membership ──1:N──> lesson_pass ──1:N──> lesson_pass_ledger
recurring_schedule ──1:N──> lesson ──1:N──> lesson_participant
lesson_participant: FK 3개 (lesson_id, center_membership_id, lesson_pass_id)
```

**어제 구조에서 교정된 것**
| 어제 | 오늘 | 이유 |
|---|---|---|
| `member` | `account` + `membership` | 사람 ≠ 센터별 소속 (윤다리 다중 센터) |
| `member_role` | `membership_role` | 역할은 센터마다 다르다 |
| `lesson_pass.member_id` | `lesson_pass.center_membership_id` | 수업권은 센터별 분리 — 안 그러면 테넌트 격리가 뚫린다 |

**HANDOFF ERD 초안(11개)과 비교**
- 아직 못 찾음: `recurring_enrollment`, `schedule_assignment`
- **초안에 없는데 직접 찾아낸 것: `membership_role`** (기획서 3.3 "Role 복수 보유"를 만족하려면 필요)

## 3교시 — 남은 2개 도출, ERD 완성 (2026-08-09)

**배운 규칙**
- 테이블 경계를 정하는 질문: **"이 사실은 무엇에 대한 사실인가?"**
- 같은 값이 여러 줄에 복사되면 그건 **아직 이름 없는 단위가 숨어 있다는 신호** (1교시 💣1번과 같은 폭탄)
- 같은 테이블을 두 번 참조해도 된다 — 단 **컬럼 이름으로 역할을 구분**한다 (`coach_membership_id`)
- 파생 가능한 FK는 중복 저장하지 않는다 (`schedule_assignment`에서 `center_membership_id` 제거)

**도출한 2개**
```
schedule_assignment   recurring_schedule_id + recurring_enrollment_id
                       └ 1인=1줄, 2인=2줄, 주2회=2줄. 구조 하나로 전부 표현
recurring_enrollment     center_membership_id + 상태 + 홀드종료일 + grace종료일 + 현재/다음 pass
                       └ 홀드·Renewal Grace가 사는 곳 (기획서 4.2 / CLAUDE.md 용어 규칙)
```

**왜 recurring_enrollment가 필요했나 (반례 2개)**
| 홀드를 어디 두면 | 무엇이 깨지나 |
|---|---|
| `recurring_schedule` | 김상진만 홀드인데 **김아들까지 멈춘다** (한 슬롯 공유) |
| `schedule_assignment` | 김서연 주 2회 → **같은 홀드 정보가 두 줄에 복사**, 하나만 고치면 유령 상태 |

→ 홀드는 "회원의 정기 이용 관계 전체"에 대한 사실. 그래서 그 단위를 테이블로 만들었다.

**최종 11개**
```
[사람·테넌트]
  account ──1:N──> membership <──N:1── center
                        ├──1:N──> membership_role
                        ├──1:N──> lesson_pass ──1:N──> lesson_pass_ledger
                        │              ↑ N:1
                        │           product <──1:N── center
                        └──1:N──> recurring_enrollment
                                          │ 1:N
  recurring_schedule ──1:N──> schedule_assignment
         │ 1:N
      lesson ──1:N──> lesson_participant ──> membership, lesson_pass
```
HANDOFF ERD 초안 11개와 일치 + `membership_role`은 초안에 없던 것을 직접 도출.

**면접 답변 3개 (완성 — 말로 할 수 있어야 함)**
- **잔여를 왜 컬럼으로 안 두나** → 숫자만 저장하면 **왜 그 숫자인지 설명할 수 없다.** 발급·출석·노쇼·복구·만료를 트랜잭션으로 쌓고 잔여는 합계. 노쇼 복구가 있어 회원 문의가 실제로 발생하고, **이력은 나중에 추가할 수 없는 데이터**라 처음부터 넣었다.
- **멀티테넌트 격리** → 로그인 정보만 `account`, 도메인 데이터는 전부 `membership`. 수업권을 계정에 붙이면 B센터에서 산 걸로 A센터를 이용 → 정산 사고.
- **홀드 위치** → 스케줄에 두면 2인 레슨이 깨지고, 참가자에 두면 주 2회가 중복. 그래서 정기 등록 단위를 따로 뒀다.

## 아직 안 푼 문제

- ~~**윤다리(다중 센터 회원)**~~ → 2교시에서 `account` + `membership` 분리로 해결 (기획서 3.2)
- `슬롯`이 테이블인가? 기획서 4.4의 "빈 시간 = 근무시간 − (스케줄 + 예약 + 닫은구간)"은 계산 결과 → **보류 유지**
- `lesson`의 수동 수정 여부(Materializer 덮어쓰기 금지)를 어떤 컬럼으로 표현할지 → DDL 단계에서 결정

## 다음에 할 것

**22일 커리큘럼으로 재시작했다 (2026-08-22).**
- 기획 정본: `docs/기획서_v2.md` (2026-08-24 재수립)
- 설계: `docs/superpowers/specs/2026-08-22-22일-완성-커리큘럼-design.md`
- 실행: `docs/superpowers/plans/2026-08-22-slice1-뼈대와-배포.md`

| | 내용 |
|---|---|
| 지금 | **Task 3 완료** — 테이블 4개, 테스트 14개 통과 |
| 다음 | **Task 4** — OTP·이메일 인증과 토큰 발급 |
| 이번 슬라이스 목표 | `git push` → AWS 자동 배포 → `https://www.fortyall.net` |

Task별 회고는 `W1_SETUP/T{번호}_정리.md` 에 남긴다.

> ERD 워크시트(`W1_ERD/`)는 삭제했다. 도출 과정과 결론은 위 "1~3교시" 절에 남아 있고,
> 확정된 12개 테이블 구조는 `docs/기획서_v2.md` 와 `docs/네이밍_규칙.md` 가 보관한다.
