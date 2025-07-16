package com.tianqueal.flowjet.backend.repositories

import com.tianqueal.flowjet.backend.domain.entities.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserRepository :
    JpaRepository<UserEntity, Long>,
    JpaSpecificationExecutor<UserEntity> {
    @EntityGraph(attributePaths = ["roles"])
    override fun findAll(
        spec: Specification<UserEntity>?,
        pageable: Pageable,
    ): Page<UserEntity>

    @EntityGraph(attributePaths = ["roles"])
    fun findWithRolesById(id: Long): UserEntity?

    fun findByEmail(email: String): UserEntity?

    @EntityGraph(attributePaths = ["roles"])
    fun findWithRolesByEmail(email: String): UserEntity?

    fun findByUsername(username: String): UserEntity?

    @EntityGraph(attributePaths = ["roles"])
    fun findWithRolesByUsername(username: String): UserEntity?

//    fun findByUsernameOrEmail(
//        username: String,
//        email: String,
//    ): UserEntity?

    @EntityGraph(attributePaths = ["roles"])
    fun findWithRolesByUsernameOrEmail(
        username: String,
        email: String,
    ): UserEntity?

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    // @Modifying
    // @Transactional
    // @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    // fun hardDeleteById(id: Long)

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users", nativeQuery = true)
    fun hardDeleteAll()

    // @Transactional(readOnly = true)
    // @Query(value = "SELECT u FROM UserEntity u WHERE u.deletedAt IS NOT NULL")
    // fun findAllDeleted(): Set<UserEntity>

    // @Transactional(readOnly = true)
    // @Query(value = "SELECT u FROM UserEntity u WHERE u.id = :id")
    // fun findByIdIncludingDeleted(id: Long): UserEntity?
}
