#!/usr/bin/env bash
# MySQL 접속 헬퍼.
#   charset을 지정하지 않으면 한글이 물음표로 보인다.
#   비밀번호는 MYSQL_PWD로 넘겨 경고를 피한다.
#
#   ./scripts/db.sh                          대화형 접속
#   ./scripts/db.sh "select * from account"  단일 쿼리
#   ./scripts/db.sh --tables                 테이블별 실제 행 수
#   ./scripts/db.sh --ledger 1               수업권 1번 원장
#   ./scripts/db.sh --schema                 현재 스키마 DDL 덤프
#   ./scripts/db.sh --reset                  모든 행 삭제 (스키마는 유지)
set -euo pipefail
C=fortyall-mysql
DB=fortyall_desk

docker inspect "$C" >/dev/null 2>&1 || { echo "컨테이너 $C 가 없습니다. docker compose up -d 를 먼저 실행하세요."; exit 1; }
[ "$(docker inspect -f '{{.State.Running}}' "$C")" = "true" ] || { echo "컨테이너가 멈춰 있습니다. docker compose up -d 를 실행하세요."; exit 1; }

q() { docker exec -i -e MYSQL_PWD=fortyall "$C" mysql --default-character-set=utf8mb4 -ufortyall "$DB" "$@"; }

TABLES="account center membership membership_role product lesson_pass pass_transaction
        standing_schedule regular_enrollment schedule_participant lesson lesson_participant
        idempotency_record"

case "${1:-}" in
  "")
    exec docker exec -it -e MYSQL_PWD=fortyall "$C" mysql --default-character-set=utf8mb4 -ufortyall "$DB"
    ;;
  --tables)
    # information_schema.table_rows는 InnoDB에서 추정치라 작은 테이블에 0이 찍힌다. count(*)로 센다.
    SQL=""
    for t in $TABLES; do
      [ -n "$SQL" ] && SQL="$SQL union all "
      SQL="${SQL}select '$t' as t, count(*) as c from $t"
    done
    printf "\n%-24s %s\n" "테이블" "행 수"
    printf '%.0s─' {1..34}; echo
    q -N -e "$SQL" | awk -F'\t' '{printf "%-24s %s\n", $1, $2}'
    ;;
  --ledger)
    PASS_ID="${2:?사용법: ./scripts/db.sh --ledger <passId>}"
    q -e "
      select t.id, t.type as 사유, t.delta as 증감, t.memo as 메모, t.created_at as 발생시각
        from pass_transaction t
       where t.lesson_pass_id = ${PASS_ID}
       order by t.id;
      select sum(delta) as 현재_잔여 from pass_transaction where lesson_pass_id = ${PASS_ID};"
    ;;
  --reset)
    # 스키마는 두고 데이터만 비운다. FK 때문에 삭제 순서를 신경 써야 하므로 체크를 잠시 끈다.
    SQL="SET FOREIGN_KEY_CHECKS=0;"
    for t in $TABLES; do SQL="$SQL truncate table $t;"; done
    SQL="$SQL SET FOREIGN_KEY_CHECKS=1;"
    q -e "$SQL"
    echo "데이터를 모두 비웠습니다 (테이블 구조는 그대로)."
    ;;
  --schema)
    docker exec -i -e MYSQL_PWD=fortyall "$C" \
      mysqldump -ufortyall --no-data --no-tablespaces --skip-comments --compact "$DB"
    ;;
  *)
    q -e "$1"
    ;;
esac
