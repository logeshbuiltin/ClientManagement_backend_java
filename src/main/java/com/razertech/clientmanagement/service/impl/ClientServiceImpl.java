package com.razertech.clientmanagement.service.impl;

import com.razertech.clientmanagement.dto.ClientDto;
import com.razertech.clientmanagement.entity.Client;
import com.razertech.clientmanagement.entity.Role;
import com.razertech.clientmanagement.repository.ClientRepository;
import com.razertech.clientmanagement.repository.RoleRepository;
import com.razertech.clientmanagement.service.ClientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private ClientRepository clientRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public ClientServiceImpl(ClientRepository clientRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(ClientDto userDto) {
        Client client = new Client();
        client.setName(userDto.getFirstName() + " " + userDto.getLastName());
        client.setEmail(userDto.getEmail());
        client.setContactNo(userDto.getContactNo());
        client.setMobileNo(userDto.getMobileNo());
        client.setCompanyName(userDto.getCompanyName());

        //encrypt the password once we integrate spring security
        //user.setPassword(userDto.getPassword());
        client.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Role role = roleRepository.findByName("ROLE_ADMIN");
        if(role == null){
            role = checkRoleExist();
        }
        client.setRoles(Arrays.asList(role));
        clientRepository.save(client);
    }

    @Override
    public void updateUser(Client user) {
        clientRepository.save(user);
    }

    @Override
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    @Override
    public Client findByContact(String contactNo) {return clientRepository.findByContactNo(contactNo);}

    @Override
    public List<ClientDto> findAllUsers() {
        List<Client> users = clientRepository.findAll();
        return users.stream().map((user) -> convertEntityToDto(user))
                .collect(Collectors.toList());
    }

    private ClientDto convertEntityToDto(Client user){
        ClientDto userDto = new ClientDto();
        String[] name = user.getName().split(" ");
        userDto.setFirstName(name[0]);
        userDto.setLastName(name[1]);
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    private Role checkRoleExist() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        return roleRepository.save(role);
    }
}
