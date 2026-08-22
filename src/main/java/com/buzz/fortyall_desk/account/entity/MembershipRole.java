package com.buzz.fortyall_desk.account.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "membership_role",
       uniqueConstraints = @UniqueConstraint(name = "uk_membership_role",
                                             columnNames = {"membership_id", "role"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    MembershipRole(Membership membership, Role role) {
        this.membership = membership;
        this.role = role;
    }
}
