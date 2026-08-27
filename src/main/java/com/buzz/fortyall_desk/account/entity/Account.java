package com.buzz.fortyall_desk.account.entity;

import com.buzz.fortyall_desk.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_accounts_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_accounts_phone_number", columnNames = "phone_number")
})
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String email;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    private Account(String email, String passwordHash, String phoneNumber) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
    }

    public static Account ofStaff(String email, String passwordHash) {
        return new Account(email, passwordHash, null);
    }

    public static Account ofMember(String phoneNumber) {
        return new Account(null, null, phoneNumber);
    }

    public enum AccountStatus {
        ACTIVE,
        WITHDRAWN
    }
}
