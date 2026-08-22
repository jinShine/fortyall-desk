#!/usr/bin/env bash
# 코어 플로우 End-to-End 점검.
#   센터 개설 → 상품 → 회원 → 수업권 발급 → 고정 스케줄 → 레슨 생성 → 출석 → 원장 확인
#
# 사용법: docker compose up -d  →  ./gradlew bootRun  →  ./scripts/e2e.sh
#   기본으로 DB를 비우고 시작한다. --keep 을 붙이면 기존 데이터를 유지한다.
set -uo pipefail

BASE="${BASE:-http://localhost:8080}"
J="Content-Type: application/json"

B=$'\033[1m'; G=$'\033[32m'; Y=$'\033[33m'; C=$'\033[36m'; R=$'\033[31m'; N=$'\033[0m'

step() { echo; echo "${B}${C}━━━ $* ${N}"; }
note() { echo "   ${Y}↳ $*${N}"; }
pass() { echo "   ${G}✓ $*${N}"; }
fail() { echo "${R}✗ $*${N}"; exit 1; }

jqv() { python3 -c "import sys,json;d=json.load(sys.stdin);
def g(o,p):
    for k in p.split('.'):
        o = o[int(k)] if k.isdigit() else o[k]
    return o
print(g(d,'$1'))" 2>/dev/null; }

api() { # api METHOD PATH [BODY] [TOKEN] [IDEMKEY]
  local m=$1 p=$2 body=${3:-} tok=${4:-} idem=${5:-}
  local args=(-s -X "$m" "$BASE$p" -H "$J")
  [[ -n $tok  ]] && args+=(-H "Authorization: Bearer $tok")
  [[ -n $idem ]] && args+=(-H "Idempotency-Key: $idem")
  [[ -n $body ]] && args+=(-d "$body")
  curl "${args[@]}"
}

curl -s -o /dev/null --max-time 3 "$BASE/api/auth/otp/request" -X POST -H "$J" -d '{"phone":"000"}' \
  || fail "서버가 응답하지 않습니다. ./gradlew bootRun 을 먼저 실행하세요."

if [[ "${1:-}" == "--keep" ]]; then
  echo "${Y}기존 데이터를 유지한 채 실행합니다.${N}"
else
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  if [[ -x "$SCRIPT_DIR/db.sh" ]]; then
    "$SCRIPT_DIR/db.sh" --reset >/dev/null 2>&1 \
      && echo "${Y}DB를 비우고 시작합니다. (유지하려면 --keep)${N}" \
      || echo "${Y}DB 초기화를 건너뜁니다 (MySQL 컨테이너 없음).${N}"
  fi
fi

TODAY_DOW=$(python3 -c "import datetime;print(datetime.date.today().strftime('%A').upper())")

step "1. 센터 셀프 가입 (기획서 6.1)"
api POST /api/auth/otp/request '{"phone":"01011112222"}' >/dev/null
RES=$(api POST /api/auth/centers/signup \
  '{"phone":"01011112222","code":"123456","centerName":"포티올 테니스","adminName":"김대표"}')
ADMIN_TOKEN=$(echo "$RES" | jqv 'data.accessToken')
COACH_ID=$(echo "$RES" | jqv 'data.current.membershipId')
[[ -n ${ADMIN_TOKEN:-} && $ADMIN_TOKEN != None ]] || fail "가입 실패: $RES"
note "센터 생성 + ADMIN·COACH 멤버십 / 코치 멤버십 id=$COACH_ID"

step "2. 상품 등록 (기획서 4.3)"
PRODUCT_ID=$(api POST /api/admin/products \
  '{"name":"20분 10회권","durationMinutes":20,"capacity":1,"sessionCount":10,"validDays":90,"price":400000,"description":"환불 규정 별도"}' \
  "$ADMIN_TOKEN" | jqv 'data.productId')
note "상품 id=$PRODUCT_ID"

step "3. 회원 생성 (기획서 6.2)"
RES=$(api POST /api/admin/members '{"name":"박서연","contactPhone":"01033334444"}' "$ADMIN_TOKEN")
MEMBER_ID=$(echo "$RES" | jqv 'data.membershipId')
note "멤버십 id=$MEMBER_ID / status=$(echo "$RES" | jqv 'data.status')"

step "4. 수업권 발급 — Ledger (기획서 12장 원칙 1)"
IDEM="pay-$(date +%s)"
RES=$(api POST /api/admin/passes \
  "{\"membershipId\":$MEMBER_ID,\"productId\":$PRODUCT_ID,\"paidAmount\":400000}" "$ADMIN_TOKEN" "$IDEM")
PASS_ID=$(echo "$RES" | jqv 'data.passId')
note "$(echo "$RES" | jqv 'data.message')"

PASS_ID2=$(api POST /api/admin/passes \
  "{\"membershipId\":$MEMBER_ID,\"productId\":$PRODUCT_ID,\"paidAmount\":400000}" \
  "$ADMIN_TOKEN" "$IDEM" | jqv 'data.passId')
[[ "$PASS_ID" == "$PASS_ID2" ]] \
  && pass "동일 Idempotency-Key 재요청 — Pass 중복 발급 없음 (id=$PASS_ID)" \
  || fail "멱등성 실패: $PASS_ID vs $PASS_ID2"

step "5. 고정 스케줄 + 참가자 (기획서 4.2)"
SCHEDULE_ID=$(api POST /api/admin/schedules \
  "{\"coachMembershipId\":$COACH_ID,\"productId\":$PRODUCT_ID,\"dayOfWeek\":\"$TODAY_DOW\",\"startTime\":\"20:00\"}" \
  "$ADMIN_TOKEN" | jqv 'data.scheduleId')
note "스케줄 id=$SCHEDULE_ID ($TODAY_DOW 20:00)"
ENROLLMENT_ID=$(api POST "/api/admin/schedules/$SCHEDULE_ID/participants" \
  "{\"membershipId\":$MEMBER_ID}" "$ADMIN_TOKEN" | jqv 'data.participants.0.enrollmentId')
note "정기 등록 id=$ENROLLMENT_ID 연결"

step "6. Materializer (기획서 12장 원칙 4)"
RES=$(api POST /api/admin/schedules/materialize '' "$ADMIN_TOKEN" "mat-$(date +%s)")
note "$(echo "$RES" | jqv 'data.message')"
RES=$(api POST /api/admin/schedules/materialize '' "$ADMIN_TOKEN" "mat2-$(date +%s)")
CREATED=$(echo "$RES" | jqv 'data.lessonsCreated')
[[ "$CREATED" == "0" ]] \
  && pass "배치 재실행 — 신규 0건 / 건너뜀 $(echo "$RES" | jqv 'data.lessonsSkipped')건" \
  || fail "멱등성 실패: 신규 ${CREATED}건 중복 생성"

step "7. QR 출석 (기획서 4.5)"
api POST /api/auth/otp/request '{"phone":"01033334444"}' >/dev/null
MEMBER_TOKEN=$(api POST /api/auth/otp/verify '{"phone":"01033334444","code":"123456"}' | jqv 'data.accessToken')
[[ -n ${MEMBER_TOKEN:-} && $MEMBER_TOKEN != None ]] || fail "회원 로그인 실패"
note "첫 OTP 인증 → 계정 생성 + 멤버십 활성화"
QR=$(api POST /api/me/qr '' "$MEMBER_TOKEN" | jqv 'data.token')
AIDEM="scan-$(date +%s)"
RES=$(api POST /api/coach/attendances "{\"qrToken\":\"$QR\"}" "$ADMIN_TOKEN" "$AIDEM")
note "$(echo "$RES" | jqv 'data.message')"
REMAIN1=$(echo "$RES" | jqv 'data.remaining')
REMAIN2=$(api POST /api/coach/attendances "{\"qrToken\":\"$QR\"}" "$ADMIN_TOKEN" "$AIDEM" | jqv 'data.remaining')
[[ "$REMAIN1" == "$REMAIN2" ]] \
  && pass "중복 스캔 — 잔여 $REMAIN2회 유지, 2회 차감 없음" \
  || fail "중복 차감: $REMAIN1 → $REMAIN2"

step "8. 원장 조회 (CLAUDE.md 불변식 1)"
api POST "/api/admin/passes/$PASS_ID/restore" '{"memo":"노쇼 차감 복구"}' "$ADMIN_TOKEN" >/dev/null
api POST "/api/admin/passes/$PASS_ID/adjust" '{"delta":-1,"memo":"정산 보정"}' "$ADMIN_TOKEN" >/dev/null
api GET "/api/admin/passes/$PASS_ID/transactions" '' "$ADMIN_TOKEN" | python3 -c "
import sys, json
d = json.load(sys.stdin)['data']
print()
print('   수업권 #%s  %s' % (d['passId'], d['productName']))
print('   ┌──────────────────┬──────┬──────┬──────────────────────────────┐')
print('   │ 사유             │ 증감 │ 잔액 │ 메모                         │')
print('   ├──────────────────┼──────┼──────┼──────────────────────────────┤')
for t in d['transactions']:
    print('   │ %-16s │ %+4d │ %4d │ %-28s │' % (t['type'], t['delta'], t['balanceAfter'], (t['memo'] or '')[:28]))
print('   └──────────────────┴──────┴──────┴──────────────────────────────┘')
print('   잔여 %d회 = 위 증감의 합' % d['remaining'])
"

step "9. 테넌트 격리 (CLAUDE.md 불변식 5)"
api POST /api/auth/otp/request '{"phone":"01055556666"}' >/dev/null
OTHER_TOKEN=$(api POST /api/auth/centers/signup \
  '{"phone":"01055556666","code":"123456","centerName":"다른 테니스장","adminName":"이대표"}' | jqv 'data.accessToken')
CODE=$(api GET "/api/admin/passes/$PASS_ID/transactions" '' "$OTHER_TOKEN" | jqv 'error.code')
[[ "$CODE" == "TENANT_VIOLATION" ]] \
  && pass "타 센터 관리자의 원장 조회 차단 (TENANT_VIOLATION)" \
  || fail "테넌트 격리 실패: $CODE"

echo
echo "${B}${G}━━━ 전 구간 통과 ━━━${N}"
