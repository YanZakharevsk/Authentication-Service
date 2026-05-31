package com.innowise.auth_service.security;

import com.innowise.auth_service.jpa.entity.Credentials;
import com.innowise.auth_service.jpa.repository.CredentialsRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CredentialsRepository credentialsRepository;

    public CustomUserDetailsService(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Credentials credentials = credentialsRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + login));

        return new User(
                credentials.getLogin(),
                credentials.getPassword(),
                List.of(new SimpleGrantedAuthority(credentials.getUserRole().name()))
        );
    }
}
