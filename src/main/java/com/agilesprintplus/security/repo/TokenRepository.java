package com.agilesprintplus.security.repo;

import com.agilesprintplus.security.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface TokenRepository extends JpaRepository<Token, UUID> {

  @Query("""
    SELECT t FROM Token t
    WHERE t.user.id = :userId
      AND t.expired = false
      AND t.revoked = false
  """)
  List<Token> findAllValidTokenByUser(@Param("userId") UUID userId);

  Optional<Token> findByToken(String token);
}