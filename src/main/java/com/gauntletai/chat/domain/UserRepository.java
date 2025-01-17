package com.gauntletai.chat.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByAuth0Id(String auth0Id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByIsAiAndUsernameContainingIgnoreCase(boolean isAi, String searchTerm);
} 
