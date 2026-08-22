package com.buzz.fortyall_desk.account.entity;

import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "membership",
       uniqueConstraints = @UniqueConstraint(name = "uk_membership_center_phone",
                                             columnNames = {"center_id", "contact_phone"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.PENDING;

    @OneToMany(mappedBy = "membership", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MembershipRole> roles = new ArrayList<>();

    public Membership(Center center, String name, String contactPhone) {
        this.center = center;
        this.name = name;
        this.contactPhone = contactPhone;
    }

    public void addRole(Role role) {
        this.roles.add(new MembershipRole(this, role));
    }

    public boolean hasRole(Role role) {
        return roles.stream().anyMatch(r -> r.getRole() == role);
    }

    public void activate(Account account) {
        this.account = account;
        this.status = MembershipStatus.ACTIVE;
    }

    public enum MembershipStatus { PENDING, ACTIVE, DORMANT, WITHDRAWN }
}
