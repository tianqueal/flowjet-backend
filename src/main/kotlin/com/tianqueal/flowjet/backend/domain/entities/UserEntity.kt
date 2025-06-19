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
import java.time.Instant
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UpdateTimestamp

@Schema(description = "User entity representing a registered user in the system")
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class UserEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @Column(name = "username", nullable = false, unique = true, length = 50)
  var username: String,

  @Column(name = "email", nullable = false, unique = true, length = 255)
  var email: String,

  @Column(name = "name", nullable = false, length = 100)
  var name: String,

  @Column(name = "password_hash", nullable = false, length = 255)
  var passwordHash: String,

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "user_roles",
    joinColumns = [JoinColumn(name = "user_id")],
    inverseJoinColumns = [JoinColumn(name = "role_id")]
  )
  var roles: Set<RoleEntity> = emptySet(),

  @Column(name = "avatar_url", length = 255)
  var avatarUrl: String? = null,

  @Schema(
    description = "Timestamp when the user account was verified",
    example = "2020-01-01T00:00:00Z",
    nullable = true,
    defaultValue = "null"
  )
  @Column(name = "verified_at")
  var verifiedAt: Instant? = null,

  @Schema(
    description =
      "Indicates when the user account itself expires. Used for temporary accounts (e.g., trials, contracts). If null, the account never expires.",
    example = "2020-01-01T00:00:00Z",
    nullable = true,
    defaultValue = "null"
  )
  @Column(name = "account_expired_at")
  var accountExpiredAt: Instant? = null,
  @Schema(
    description =
      "Indicates when the user account was locked, typically for security reasons like multiple failed login attempts. If null, the account is not locked.",
    example = "2020-01-01T00:00:00Z",
    nullable = true,
    defaultValue = "null"
  )
  @Column(name = "locked_at")
  var lockedAt: Instant? = null,

  @Schema(
    description =
      "Indicates when the user's credentials (e.g., password) expire, forcing a change. If null, the credentials never expire.",
    example = "2020-01-01T00:00:00Z",
    nullable = true,
    defaultValue = "null"
  )
  @Column(name = "credentials_expired_at")
  var credentialsExpiredAt: Instant? = null,

  @Schema(
    description =
      "Master switch indicating if the user is administratively disabled. Stores the timestamp of when the disabling action occurred. If null, the user is enabled.",
    example = "2020-01-01T00:00:00Z",
    nullable = true,
    defaultValue = "null"
  )
  @Column(name = "disabled_at")
  var disabledAt: Instant? = null,

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  var createdAt: Instant? = null,

  @UpdateTimestamp @Column(name = "updated_at", nullable = false)
  var updatedAt: Instant? = null,

  @Column(name = "deleted_at")
  var deletedAt: Instant? = null
) {
  fun isVerified(): Boolean = verifiedAt != null

  fun isAccountNonExpired(): Boolean {
    return accountExpiredAt?.isAfter(Instant.now()) ?: true
  }

  fun isAccountNonLocked(): Boolean = lockedAt == null

  fun isCredentialsNonExpired(): Boolean {
    return credentialsExpiredAt?.isAfter(Instant.now()) ?: true
  }

  fun isDeleted(): Boolean = deletedAt != null

  fun markAsDeleted() {
    deletedAt = Instant.now()
  }

  fun restore() {
    deletedAt = null
  }
}
