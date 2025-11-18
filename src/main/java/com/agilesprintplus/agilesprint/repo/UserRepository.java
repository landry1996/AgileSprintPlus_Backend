package com.agilesprintplus.agilesprint.repo;


import com.agilesprintplus.agilesprint.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameIgnoreCase(@Param("username") String username);
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    Page<User> findAllActive(Pageable pageable);
    @Query("SELECT u FROM User u WHERE u.enabled = false")
    Page<User> findAllDisabled(Pageable pageable);

    @EntityGraph(attributePaths = "roles")
    @Query("select u from User u order by u.createdAt desc")
    List<User> findAllWithRoles();
}


