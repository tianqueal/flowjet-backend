package com.tianqueal.flowjet.backend.domain.entities

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Schema(description = "User entity representing a registered user in the system")
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class UserEntity(
    @field:Schema(description = "Unique identifier of the user")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Schema(description = "Username of the user")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    var username: String,
    @field:Schema(description = "User email address")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String,
    @field:Schema(description = "Full name of the user")
    @Column(name = "name", nullable = false, length = 100)
    var name: String,
    @field:Schema(description = "Password hashed of the user")
    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,
    @field:Schema(description = "User's roles")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    var roles: MutableSet<RoleEntity> = mutableSetOf(),
//    @field:Schema(description = "List of project memberships for the user")
//    @OneToMany(
//        mappedBy = "user",
//        fetch = FetchType.LAZY,
//    )
//    var projectsMemberships: MutableSet<ProjectMemberEntity> = mutableSetOf(),
    @field:Schema(description = "URL of the user's avatar image", nullable = true)
    @Column(name = "avatar_url", length = 255)
    var avatarUrl: String? = null,
    @field:Schema(
        description = "Timestamp when the user account was verified",
        nullable = true,
    )
    @Column(name = "verified_at")
    var verifiedAt: Instant? = null,
    @field:Schema(
        description =
            "Indicates when the user account itself expires. Used for temporary accounts (e.g., trials, contracts). If null, the account never expires.",
        nullable = true,
    )
    @Column(name = "account_expired_at")
    var accountExpiredAt: Instant? = null,
    @field:Schema(
        description =
            "Indicates when the user account was locked, typically for security reasons like multiple failed login attempts. If null, the account is not locked.",
        nullable = true,
    )
    @Column(name = "locked_at")
    var lockedAt: Instant? = null,
    @field:Schema(
        description =
            "Indicates when the user's credentials (e.g., password) expire, forcing a change. If null, the credentials never expire.",
        nullable = true,
    )
    @Column(name = "credentials_expired_at")
    var credentialsExpiredAt: Instant? = null,
    @field:Schema(
        description =
            "Master switch indicating if the user is administratively disabled. Stores the timestamp of when the disabling action occurred. If null, the user is enabled.",
        nullable = true,
    )
    @Column(name = "disabled_at")
    var disabledAt: Instant? = null,
    @field:Schema(description = "Creation timestamp")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @field:Schema(description = "Last update timestamp")
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    @field:Schema(description = "Logical deletion timestamp (null if not deleted)", nullable = true)
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
) {
    val safeId: Long
        get() =
            id
                ?: throw IllegalStateException("ID not initialized for $this")

    fun isVerified(): Boolean = verifiedAt != null

    fun isAccountNonExpired(): Boolean = accountExpiredAt?.isAfter(Instant.now()) ?: true

    fun isAccountNonLocked(): Boolean = lockedAt == null

    fun isCredentialsNonExpired(): Boolean = credentialsExpiredAt?.isAfter(Instant.now()) ?: true

    fun isEnabled(): Boolean = disabledAt == null

    fun isDeleted(): Boolean = deletedAt != null

    //  fun markAsDeleted() {
    //    deletedAt = Instant.now()
    //  }

    //  fun restore() {
    //    deletedAt = null
    //  }
}
