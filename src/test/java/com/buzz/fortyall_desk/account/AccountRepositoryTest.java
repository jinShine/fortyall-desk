package com.buzz.fortyall_desk.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buzz.fortyall_desk.account.entity.Account;
import com.buzz.fortyall_desk.account.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired AccountRepository accountRepository;

    @Test
    @DisplayName("관리자 계정은 이메일과 비밀번호를 갖고 번호는 없다")
    void 관리자_계정은_이메일로_만들어진다() {
        Account 김대표 = accountRepository.save(Account.ofStaff("kim@greencourt.com", "해시값"));

        assertThat(accountRepository.findByEmail("kim@greencourt.com")).isPresent();
        assertThat(김대표.getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("회원 계정은 번호만 갖고 이메일·비밀번호는 없다 — 계정을 만들지 않는다")
    void 회원_계정은_번호로_만들어진다() {
        Account 김서연 = accountRepository.save(Account.ofMember("01055556666"));

        assertThat(accountRepository.findByPhoneNumber("01055556666")).isPresent();
        assertThat(김서연.getEmail()).isNull();
        assertThat(김서연.getPasswordHash()).isNull();
    }

    @Test
    @DisplayName("없는 이메일·번호를 조회하면 비어 있다")
    void 없는_계정은_빈_결과다() {
        assertThat(accountRepository.findByEmail("nobody@test.com")).isEmpty();
        assertThat(accountRepository.findByPhoneNumber("01000000000")).isEmpty();
    }

    @Test
    @DisplayName("같은 이메일로 두 계정을 만들 수 없다 — 로그인 신원이 유일해야 한다")
    void 이메일은_중복될_수_없다() {
        accountRepository.save(Account.ofStaff("kim@greencourt.com", "해시값"));

        assertThatThrownBy(() -> accountRepository.saveAndFlush(
                Account.ofStaff("kim@greencourt.com", "다른해시")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("같은 번호로 두 계정을 만들 수 없다 — 재로그인의 열쇠가 유일해야 한다")
    void 전화번호는_중복될_수_없다() {
        accountRepository.save(Account.ofMember("01055556666"));

        assertThatThrownBy(() -> accountRepository.saveAndFlush(
                Account.ofMember("01055556666")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
