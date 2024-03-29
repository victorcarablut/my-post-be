package com.victorcarablut.code.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.victorcarablut.code.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	@Query(value = "SELECT u.email FROM users u WHERE u.email = :email", nativeQuery = true)
    Map<String, Object> findByEmailAndReturnOnlyEmail(String email);
	
	@Query(value = "SELECT u.id FROM users u WHERE u.username = :username", nativeQuery = true)
    Long findUserIdByUsernameAndReturnOnlyUserId(String username);
	
	User findByEmail(String email);

	User findUserById(Long id);
	
	Boolean existsUserById(Long id);
	
	Boolean existsUserByEmail(String email);
	
	Boolean existsUserByUsername(String username);
	
	Optional<User> findByUsername(String username);
	
	Optional<User> findUsernameByEmail(String email);
	
	Optional<User> findOneByEmail(String email);
	
	Boolean existsUserByEmailAndVerificationCode(String email, Integer code);

	User findByPassword(String password);

	User findUserByUsername(String username);
	

}
