package org.example.digital_banking.repositories;

import org.example.digital_banking.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepo extends JpaRepository<BankAccount, Long> {
    @Query("SELECT a FROM BankAccount a WHERE a.customer.customer_id = :customerId")
    List<BankAccount> findAccountsByCustomerId(@Param("customerId") Long customerId);



    // You can add custom queries here if needed
}
