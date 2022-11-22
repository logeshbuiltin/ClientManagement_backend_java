package com.razertech.clientmanagement.repository;

import com.razertech.clientmanagement.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findByContactNo(String contactNo);

    Client findByEmail(String email);
}
