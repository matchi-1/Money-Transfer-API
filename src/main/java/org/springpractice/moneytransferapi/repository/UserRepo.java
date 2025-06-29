package org.springpractice.moneytransferapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springpractice.moneytransferapi.entity.User;

public interface UserRepo extends JpaRepository<User,Integer> {
}
