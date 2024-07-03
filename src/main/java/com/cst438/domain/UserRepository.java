package com.cst438.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends 
                 CrudRepository<User, Integer>{

	List<User> findAllByOrderByIdAsc();

	User findByEmail(String email);

	@Query("select u from User u where u.id=:studentId and u.type='STUDENT'")
	User findStudentById(int studentId);
}
