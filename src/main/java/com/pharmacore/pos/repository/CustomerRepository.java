package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByKraPin(String kraPin);
}

