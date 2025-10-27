package com.polstat.WebServiceApel.security.service;

import com.polstat.WebServiceApel.entity.User;
import com.polstat.WebServiceApel.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Username bisa berupa email atau username tergantung kebutuhan
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        //kita mengembalikan instance user yang sudah mengimplementasikan UserDetails
        return new CustomUserDetails(user);
    }
}
