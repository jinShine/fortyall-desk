package com.buzz.fortyall_desk.account.entity;

import com.buzz.fortyall_desk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "account",
       uniqueConstraints = @UniqueConstraint(name = "uk_account_login_phone",
                                             columnNames = "login_phone"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_phone", nullable = false, length = 20)
    private String loginPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    public Account(String loginPhone) {
        this.loginPhone = loginPhone;
    }

    public enum AccountStatus { ACTIVE, WITHDRAWN }
}
