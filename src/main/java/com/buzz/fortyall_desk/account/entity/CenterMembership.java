package com.buzz.fortyall_desk.account.entity;

import java.util.ArrayList;
import java.util.List;

import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "center_memberships", uniqueConstraints = @UniqueConstraint(name = "uk_center_memberships_center_phone", columnNames = {
        "center_id", "contact_phone_number" }))
public class CenterMembership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "contact_phone_number", nullable = false, length = 20)
    private String contactPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.PENDING;

	@OneToMany(mappedBy = "centerMembership", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MembershipRole> roles = new ArrayList<>();

    public CenterMembership(Center center, String displayName, String contactPhoneNumber) {
        this.center = center;
        this.displayName = displayName;
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public void connect(Account account) {
        this.account = account;
        this.status = MembershipStatus.ACTIVE;
    }

	public void addRole(Role role) {
		this.roles.add(new MembershipRole(this, role));
	}

	public boolean hasRole(Role role) {
		return roles.stream().anyMatch(r -> r.getRole() == role);
	}

    public enum MembershipStatus {
        ACTIVE, // 활성
        PENDING, // 가입 대기
        DORMANT, // 휴면
        WITHDRAWN // 탈퇴
    }
}
