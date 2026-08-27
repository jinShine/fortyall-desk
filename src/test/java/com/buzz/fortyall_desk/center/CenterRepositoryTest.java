package com.buzz.fortyall_desk.center;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.center.repository.CenterRepository;

@DataJpaTest
class CenterRepositoryTest {

    @Autowired
    CenterRepository centerRepository;

    @Test
    void 센터를_저장하면_id와_생성시각이_채워진다() {
        Center saved = centerRepository.save(new Center("그린코트테니스"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("그린코트테니스");
        assertThat(saved.getStatus()).isEqualTo(Center.CenterStatus.ACTIVE);
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
