#!/usr/bin/env bash
# MySQL 접속 헬퍼
#   ./scripts/db.sh                     대화형 접속
#   ./scripts/db.sh --tables            테이블 목록 + 행 수
#   ./scripts/db.sh --desc accounts     테이블 구조
#   ./scripts/db.sh "select * from centers"   쿼리 실행
#
# charset 을 지정하지 않으면 한글이 물음표로 보인다. 비밀번호는 MYSQL_PWD 로 넘겨 경고를 없앤다.
set -uo pipefail
C=fortyall-mysql
DB=fortyall_desk
PW=whis!34679

docker inspect "$C" >/dev/null 2>&1 || { echo "컨테이너 $C 가 없습니다. docker compose up -d 를 먼저 실행하세요."; exit 1; }
[ "$(docker inspect -f '{{.State.Running}}' "$C")" = "true" ] || { echo "컨테이너가 멈춰 있습니다. docker compose up -d 를 실행하세요."; exit 1; }

q() { docker exec -i -e MYSQL_PWD="$PW" "$C" mysql --default-character-set=utf8mb4 -ufortyall "$DB" "$@"; }

case "${1:-}" in
  "")
    exec docker exec -it -e MYSQL_PWD="$PW" "$C" mysql --default-character-set=utf8mb4 -ufortyall "$DB"
    ;;
  --tables)
    echo
    printf "%-26s %s\n" "테이블" "행 수"
    printf '%.0s─' {1..36}; echo
    for t in $(q -N -e "show tables;"); do
      printf "%-26s %s\n" "$t" "$(q -N -e "select count(*) from \`$t\`;")"
    done
    ;;
  --desc)
    T="${2:?사용법: ./scripts/db.sh --desc <테이블명>}"
    q -e "desc \`$T\`;"
    echo "── 인덱스·제약 ──"
    q -e "show index from \`$T\` where Non_unique = 0;" | awk 'NR==1{print "Key_name\tColumn"} NR>1{print $3"\t"$5}'
    ;;
  *)
    q -e "$1"
    ;;
esac
