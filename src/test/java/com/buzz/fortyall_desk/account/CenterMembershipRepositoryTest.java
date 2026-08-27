package com.buzz.fortyall_desk.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buzz.fortyall_desk.account.entity.Account;
import com.buzz.fortyall_desk.account.entity.CenterMembership;
import com.buzz.fortyall_desk.account.entity.CenterMembership.MembershipStatus;
import com.buzz.fortyall_desk.account.entity.Role;
import com.buzz.fortyall_desk.account.repository.AccountRepository;
import com.buzz.fortyall_desk.account.repository.CenterMembershipRepository;
import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.center.repository.CenterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class CenterMembershipRepositoryTest {

    @Autowired CenterMembershipRepository centerMembershipRepository;
    @Autowired CenterRepository centerRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    @DisplayName("한 계정이 여러 센터에 소속될 수 있다 — 윤다리")
    void 한_계정이_여러_센터에_소속될_수_있다() {
        Account 윤다리 = accountRepository.save(Account.ofMember("01055556666"));
        Center a센터 = centerRepository.save(new Center("A테니스"));
        Center b센터 = centerRepository.save(new Center("B테니스"));

        CenterMembership a소속 = new CenterMembership(a센터, "윤다리", "01055556666");
        CenterMembership b소속 = new CenterMembership(b센터, "윤다리", "01055556666");
        a소속.connect(윤다리);
        b소속.connect(윤다리);
        centerMembershipRepository.save(a소속);
        centerMembershipRepository.save(b소속);

        assertThat(centerMembershipRepository.findAllByAccountId(윤다리.getId()))
                .hasSize(2)
                .extracting(m -> m.getCenter().getName())
                .containsExactlyInAnyOrder("A테니스", "B테니스");
    }

    @Test
    @DisplayName("역할을 여러 개 가질 수 있다 — 김대표는 관리자이자 코치")
    void 역할을_여러_개_가질_수_있다() {
        Center center = centerRepository.save(new Center("그린코트테니스"));
        CenterMembership 김대표 = new CenterMembership(center, "김대표", "01011112222");
        김대표.addRole(Role.ADMIN);
        김대표.addRole(Role.COACH);

        CenterMembership saved = centerMembershipRepository.save(김대표);

        assertThat(saved.hasRole(Role.ADMIN)).isTrue();
        assertThat(saved.hasRole(Role.COACH)).isTrue();
        assertThat(saved.hasRole(Role.MEMBER)).isFalse();
        assertThat(saved.getRoles()).hasSize(2);
    }

    @Test
    @DisplayName("다른 센터의 소속은 조회되지 않는다 — 테넌트 격리")
    void 다른_센터의_소속은_조회되지_않는다() {
        Center a센터 = centerRepository.save(new Center("A테니스"));
        Center b센터 = centerRepository.save(new Center("B테니스"));
        CenterMembership a회원 = centerMembershipRepository.save(
                new CenterMembership(a센터, "김혜원", "01033334444"));

        assertThat(centerMembershipRepository.findByIdAndCenterId(a회원.getId(), a센터.getId()))
                .isPresent();
        assertThat(centerMembershipRepository.findByIdAndCenterId(a회원.getId(), b센터.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("연결 전 소속은 PENDING이고 계정이 없다 — 오할머니")
    void 연결_전_소속은_PENDING이고_계정이_없다() {
        Center center = centerRepository.save(new Center("그린코트테니스"));

        CenterMembership 오할머니 = centerMembershipRepository.save(
                new CenterMembership(center, "오할머니", "01077778888"));

        assertThat(오할머니.getStatus()).isEqualTo(MembershipStatus.PENDING);
        assertThat(오할머니.getAccount()).isNull();
    }

    @Test
    @DisplayName("초대링크로 연결하면 계정이 붙고 ACTIVE가 된다")
    void 연결하면_ACTIVE가_된다() {
        Center center = centerRepository.save(new Center("그린코트테니스"));
        Account 김혜원 = accountRepository.save(Account.ofMember("01033334444"));
        CenterMembership 소속 = new CenterMembership(center, "김혜원", "01033334444");

        소속.connect(김혜원);
        CenterMembership saved = centerMembershipRepository.save(소속);

        assertThat(saved.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(saved.getAccount().getId()).isEqualTo(김혜원.getId());
    }

    @Test
    @DisplayName("같은 센터에 같은 번호를 두 번 등록할 수 없다 — 복합 유니크")
    void 같은_센터에_같은_번호는_중복_등록되지_않는다() {
        Center center = centerRepository.save(new Center("그린코트테니스"));
        centerMembershipRepository.save(new CenterMembership(center, "김혜원", "01033334444"));

        assertThatThrownBy(() -> centerMembershipRepository.saveAndFlush(
                new CenterMembership(center, "동명이인", "01033334444")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("센터별 회원 목록은 이름순으로 정렬된다")
    void 센터별_회원_목록은_이름순_정렬된다() {
        Center center = centerRepository.save(new Center("그린코트테니스"));
        centerMembershipRepository.save(new CenterMembership(center, "김혜원", "01011110001"));
        centerMembershipRepository.save(new CenterMembership(center, "박준석", "01011110002"));
        centerMembershipRepository.save(new CenterMembership(center, "김서연", "01011110003"));

        assertThat(centerMembershipRepository.findAllByCenterIdOrderByDisplayNameAsc(center.getId()))
                .extracting(CenterMembership::getDisplayName)
                .containsExactly("김서연", "김혜원", "박준석");
    }
}
