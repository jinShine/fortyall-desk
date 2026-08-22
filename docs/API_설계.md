# FORTYALL:DESK — API 설계 v1

> 출처: [기획서_Master_v1.5](기획서_Master_v1.5.md) 10장(화면 정의) · 6장(운영 플로우) · 9장(인증) · 12장(설계 원칙)
> **기획서와 충돌하면 기획서가 이긴다.**

## 0. 공통 규약

### 0.1 경로 체계 — 역할별 prefix
```
/api/auth/**       인증 (비로그인 접근 가능)
/api/me/**         회원 본인
/api/coach/**      코치
/api/admin/**      관리자
/api/platform/**   플랫폼 운영자
```
**centerId를 경로에 넣지 않는다.** 로그인 후 발급되는 액세스 토큰이 `accountId + membershipId + centerId + roles`를
담고, 모든 쿼리는 토큰의 `centerId`로 스코프된다. 경로에 두면 `/api/centers/3/members`를 다른 센터 토큰으로
호출하는 실수가 컨트롤러마다 반복 검사 대상이 되지만, 토큰에 두면 **한 곳(인터셉터)에서 막힌다.**
→ 기획서 3.2 "데이터 격리는 멤버십 단위" · CLAUDE.md 불변식 5

### 0.2 인증 헤더
```
Authorization: Bearer {accessToken}
```
다중 소속 계정은 OTP 검증 후 **센터 선택 → 센터별 토큰 재발급**. (기획서 3.2)

### 0.3 응답 포맷
```json
// 성공
{ "data": { ... } }
// 실패
{ "error": { "code": "PASS_EXPIRED", "message": "수업권이 만료되었습니다", "detail": {...} } }
```

### 0.4 멱등성 (기획서 12장 동시성 5대)
아래 4개는 `Idempotency-Key` 헤더 **필수**. 같은 키 재요청은 최초 결과를 그대로 반환한다.
| 엔드포인트 | 막는 사고 |
|---|---|
| `POST /api/admin/passes` | 결제 승인 중복 → Pass 2개 발급 |
| `POST /api/coach/attendances` | QR 중복 스캔 → 2회 차감 |
| `POST /api/me/bookings` | 동시 예약 → 한 슬롯 2명 |
| `POST /api/admin/schedules/materialize` | 배치 중복 → 레슨 2개 |

### 0.5 구현 우선순위
- **P0** = 코어 루프 (수업권 발급 → 스케줄 → 예약 → 출석 → 원장)
- **P1** = W4~W5
- **P2** = 로드맵상 연장전 (NTRP·홀드·승계·2인·CSV)

---

## 1. 인증 · 계정 (기획서 9장)

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | POST | `/api/auth/otp/request` | 번호 입력 → OTP 6자리 발송. rate limit·일일 상한 |
| **P0** | POST | `/api/auth/otp/verify` | OTP 검증 → 소속 목록 + 임시 토큰. 첫 성공 = 활성화(회원)/가입(운영자) |
| **P0** | POST | `/api/auth/centers/{centerId}/select` | 센터 선택 → 해당 멤버십 스코프 액세스 토큰 발급 |
| P1 | POST | `/api/auth/refresh` | 토큰 갱신 ("이 기기 기억하기") |
| P1 | POST | `/api/auth/logout` | 로그아웃 |
| P1 | POST | `/api/me/phone/change` | 로그인 번호 변경 — **기존+새 번호 이중 인증**, 본인 전용 |
| P2 | DELETE | `/api/me/account` | 플랫폼 탈퇴 — 활성 멤버십·미사용 수업권·운영 역할 검증 후 (4.7) |

**`otp/verify` 응답 분기** (기획서 3.2)
```
동일 번호 계정 있음 → 그 계정에 멤버십 연결
없음              → 새 계정 생성
소속 1개          → 액세스 토큰 즉시 발급
소속 2개+         → 센터 목록 반환, select 호출 필요
```

---

## 2. 센터 온보딩 (기획서 6.1)

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | POST | `/api/auth/centers/signup` | 셀프 가입 — 번호+OTP 후 센터명 → 센터 생성 + ADMIN 멤버십 |
| P1 | GET | `/api/admin/center` | 센터 정보 조회 |
| P1 | PATCH | `/api/admin/center` | 센터 정보 수정 |
| P1 | GET | `/api/admin/center/checklist` | 초기 세팅 체크리스트 |
| P1 | GET/PUT | `/api/admin/center/policies` | 정책 카탈로그 (기획서 7장 — Booking Window, Grace 일수, 늦은취소 기준 등) |

---

## 3. 회원 관리 (기획서 6.2 · 10장 관리자)

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | POST | `/api/admin/members` | 회원 생성 (관리자 생성만 — 공개 가입 없음). 미활성 상태로 시작 |
| **P0** | GET | `/api/admin/members` | 목록. 필터: `status`, `ntrpMin/Max`, `coachId`, `q` / 정렬: `ntrp`, `name` |
| **P0** | GET | `/api/admin/members/{membershipId}` | **회원 상세 허브** — 정보+수업권+고정 스케줄+메모 |
| P1 | PATCH | `/api/admin/members/{membershipId}` | 정보 수정 (센터 연락처는 관리자 수정 가능 — 로그인 번호와 별개) |
| P1 | POST | `/api/admin/members/{membershipId}/invite` | 초대 재발송 (문자·QR·카톡) |
| P1 | PATCH | `/api/admin/members/{membershipId}/status` | 활성 ⇄ 휴면 / 센터 탈퇴 (잔여>0이면 **소멸 경고** 필수) |
| P2 | POST | `/api/admin/members/import` | CSV 업로드 → 검증 → 프리뷰 토큰 |
| P2 | POST | `/api/admin/members/import/{token}/confirm` | CSV 확정 |
| P2 | GET/POST | `/api/admin/members/{id}/ntrp` | NTRP 평가 이력 조회/기록 — **회원 본인도 조회 불가** (4.6) |

---

## 4. 상품 (기획서 4.3)

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | GET | `/api/admin/products` | 상품 목록 |
| **P0** | POST | `/api/admin/products` | 생성 — 길이·인원·가격·횟수·유효기간·설명 |
| P1 | PATCH | `/api/admin/products/{id}` | 수정 — **판매된 Pass의 스냅샷은 불변** |
| P1 | DELETE | `/api/admin/products/{id}` | 비활성 (하드 삭제 없음) |

---

## 5. 수업권 (Ledger) — 기획서 4.1 · 6.3 · 12장 원칙 1

잔여는 컬럼이 아니라 `pass_transaction`의 SUM.

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | POST | `/api/admin/passes` | **발급** (결제 확인 → 승인 + 받은 금액 기록). 재등록도 **항상 새 Pass**. `Idempotency-Key` 필수 |
| **P0** | GET | `/api/admin/members/{membershipId}/passes` | 회원의 Pass 목록 + 각 잔여/확보 |
| **P0** | GET | `/api/admin/passes/{passId}/transactions` | **원장 조회 — "왜 지금 3회인가"에 답하는 화면** |
| **P0** | GET | `/api/me/passes` | 회원 본인 수업권 (잔여·확보·만료일) |
| P1 | POST | `/api/admin/passes/{passId}/adjust` | 조정 — 관리자 임의 증감도 트랜잭션 |
| P1 | POST | `/api/admin/passes/{passId}/extend` | 유효기간 연장 — 만료분이면 `만료 복구 +N` 동반 (4.1) |
| P1 | POST | `/api/admin/passes/{passId}/restore` | 노쇼·늦은취소 차감 복구 (6.7) |
| P1 | POST | `/api/admin/passes/{passId}/cancel` | 취소 |
| P2 | POST | `/api/admin/passes/{passId}/succeed` | 선결제 승계 — 대기 → 활성, 승계 시점부터 기산 |

**`POST /api/admin/passes` 요청/응답**
```json
// 요청
{ "membershipId": 12, "productId": 3, "paidAmount": 400000,
  "validFrom": "2026-08-19", "prepaid": false }
// 응답 — 외상 Carry-over 정산이 있으면 함께 반영 (4.1)
{ "data": { "passId": 55, "status": "ACTIVE", "issued": 10,
            "carryOverDebt": 2, "usable": 8,
            "transactions": [ {"type":"ISSUE","delta":10},
                              {"type":"CARRY_OVER","delta":-2} ] } }
```

**트랜잭션 타입** (기획서 4.1)
`ISSUE` 발급 · `ATTEND` 출석 −1 · `NO_SHOW` 노쇼 · `LATE_CANCEL` 늦은취소 · `RESTORE` 복구 ·
`ADJUST` 조정 · `EXPIRE` 만료 소멸 · `EXPIRE_RESTORE` 만료 복구 · `CARRY_OVER` 이월 외상 · `DEBT_SETTLE` 외상 정산

---

## 6. 고정 스케줄 · 정기 등록 (기획서 4.2)

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | POST | `/api/admin/schedules` | 고정 스케줄 생성 — 코치·요일·시각·상품. 시간 규칙만 |
| **P0** | POST | `/api/admin/schedules/{id}/participants` | 참가자 추가 = 정기 등록 연결. **2인 레슨은 2줄** |
| **P0** | GET | `/api/admin/schedules` | 주간 스케줄 조회 (`from`, `to`, `coachId`) |
| **P0** | POST | `/api/admin/schedules/materialize` | **Materializer 수동 실행** — rolling 90일. `Idempotency-Key` |
| P1 | DELETE | `/api/admin/schedules/{id}/participants/{enrollmentId}` | 참가자 해제. **활성 0명일 때만 스케줄 종료** |
| P1 | PATCH | `/api/admin/schedules/{id}` | 변경 — `effectiveFrom` 기준. **수동 이동 레슨은 덮어쓰지 않음** |
| P1 | GET | `/api/admin/enrollments/{id}` | 정기 등록 상세 (현재/다음 Pass·상태·Grace) |
| P1 | POST | `/api/admin/enrollments/{id}/grace` | Renewal Grace 시작·연장 (기본 7일) |
| P2 | POST | `/api/admin/enrollments/{id}/hold` | 홀드 시작 — **회원별**. 전원 홀드일 때만 슬롯 잠금 |
| P2 | DELETE | `/api/admin/enrollments/{id}/hold` | 홀드 해제 |

---

## 7. 슬롯 · 예약 (기획서 4.4 · 6.5)

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | GET | `/api/me/slots` | **빈 시간 조회** = 근무시간 − (고정+예약+닫은구간). `coachId`, `date` |
| **P0** | POST | `/api/me/bookings` | **셀프 예약 — 즉시 확정 + 1회 확보.** overlap 검사, `Idempotency-Key` |
| **P0** | GET | `/api/me/lessons` | 내 레슨 (`from`,`to`) |
| P1 | DELETE | `/api/me/lessons/{id}` | 회차 취소 — 규정 시간 내=사전취소(차감 X) / 초과=늦은취소(차감) |
| P1 | POST | `/api/me/lessons/{id}/move` | **이번 주만 이동 — 즉시 확정.** 신규 확보+기존 변경을 **한 트랜잭션** |
| P1 | POST | `/api/me/schedule-change-requests` | **고정 변경 요청 — 관리자 승인 대기** |
| P1 | GET/POST | `/api/coach/working-hours` | 근무시간 등록 (요일 반복/특정일) |
| P1 | POST | `/api/coach/closed-periods` | 슬롯 닫기 (휴강과 다름 — 예약 전 시간을 막음) |

**예약 시 경고** (막지 않음 — 기획서 4.1)
```json
{ "data": { "bookingId": 88, "reserved": 1, "remaining": 2 },
  "warnings": [{ "code":"REGULAR_LESSON_SHORTAGE",
                 "message":"예정된 고정 레슨 3회가 있습니다. 이 예약 시 재등록이 필요할 수 있습니다." }] }
```

---

## 8. 출석 (기획서 4.5)

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | POST | `/api/me/qr` | **QR 토큰 발급 — 짧은 TTL 서명 토큰** |
| **P0** | POST | `/api/coach/attendances` | **QR 스캔 → 출석 확정 −1.** 멤버십·권한·대상 레슨·중복 검증. `Idempotency-Key` |
| **P0** | GET | `/api/coach/lessons` | 코치 주간 레슨 |
| P1 | PATCH | `/api/coach/lessons/{lessonId}/participants/{id}` | **수동 출석 처리 — 신뢰 경로.** 상태 5종 지정 |
| P1 | POST | `/api/admin/lessons/bulk-cancel` | 일괄 취소 · 휴강일 지정 (6.4) |
| P1 | POST | `/api/coach/leave-requests` | 휴강 신청 → 승인 대기 |

**스캔 응답 — 코치 화면에 회차·잔여 표시** (기획서 4.5, 갱신 대화 유도)
```json
{ "data": { "memberName": "박서연", "productName": "20분 10회권",
            "session": "3/5", "remaining": 2, "status": "ATTENDED" } }
```

**출석 상태 5종**: `ATTENDED` −1 · `EARLY_CANCEL` 차감 X · `LATE_CANCEL` 정책 · `NO_SHOW` 정책 · `SUSPENDED` 차감 X

---

## 9. 관리자 통합 처리 큐 (기획서 10장)

**"판단 필요한 것만"** — 정상 취소는 자동 처리+알림이라 큐에 없다.

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | GET | `/api/admin/queue` | 통합 큐 — 아래 7종 집계 |
| P1 | POST | `/api/admin/queue/schedule-changes/{id}/approve` | 고정 변경 승인 → **재검증 후 반영** |
| P1 | POST | `/api/admin/queue/schedule-changes/{id}/reject` | 거절 |
| P1 | POST | `/api/admin/queue/leave-requests/{id}/approve` | 코치 휴강 승인 |
| P1 | GET | `/api/admin/queue/unprocessed-lessons` | 미처리 레슨 — **자동 노쇼 없음** |
| P1 | GET | `/api/admin/no-shows` | 노쇼·늦은취소 **분리 집계** (6.7) |

**큐 항목 7종**: 결제 확인 · 고정 변경 승인 · 코치 휴강 승인 · 미처리 레슨 · 충전 유예 임박 · CSV 오류 · 복구 요청

---

## 10. 대시보드 · 알림

| 우선 | Method | Path | 설명 |
|:--:|---|---|---|
| **P0** | GET | `/api/me/dashboard` | 잔여·다음 레슨·유예/충전 배너 |
| P1 | GET | `/api/admin/dashboard` | **결제 승인 금액** (상품별·신규/재등록·월/반기/연). 세무상 "매출" 아님을 명시 |
| P1 | GET | `/api/me/notifications` | 알림함 |
| P1 | PATCH | `/api/me/notification-settings` | 알림 토글 |
| P1 | GET | `/api/admin/notifications/history` | 발송 이력 |
| P1 | GET/POST | `/api/admin/notices` | 공지(이미지) |

---

## 11. 스코프 아웃 — 만들지 않는 API (기획서 11장)
온라인 결제(PG) · 인앱 1:1 메시지 · 후기·평점 · 게시판 · 코트 자원 · 보호자 대리 · 기간권 · 공개 체험신청

---

## 12. 코어 플로우 점검 순서 (P0)

`scripts/e2e.sh`가 이 순서를 그대로 실행한다.

```
1. POST /api/auth/centers/signup          센터 개설
2. POST /api/admin/products               20분 10회권 등록
3. POST /api/admin/members                회원 생성
4. POST /api/admin/passes                 10회 발급  → ISSUE +10, 잔여 10
5. POST /api/admin/schedules              고정 스케줄 생성
   POST .../participants                  정기 등록 연결
6. POST /api/admin/schedules/materialize  90일치 레슨 생성
7. POST /api/me/qr → POST /api/coach/attendances
                                          출석 → ATTEND -1, 잔여 9
   ↳ 같은 Idempotency-Key로 재호출        중복 차감 없음
8. GET /api/admin/passes/{id}/transactions  원장 전체
9. 타 센터 토큰으로 8번 재호출              TENANT_VIOLATION
```

이 순서가 검증하는 설계 원칙: Ledger(12장 1) · 참가자 분리(2) · Materializer 멱등성(4) · 동시성(5) · 테넌트 격리
