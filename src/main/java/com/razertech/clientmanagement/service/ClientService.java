package com.razertech.clientmanagement.service;

import com.razertech.clientmanagement.dto.ClientDto;
import com.razertech.clientmanagement.entity.Client;

import java.util.List;

public interface ClientService {
    void saveUser(ClientDto userDto);

    void updateUser(Client user);

    Client findByEmail(String email);

    Client findByContact(String contactNo);

    List<ClientDto> findAllUsers();
}
