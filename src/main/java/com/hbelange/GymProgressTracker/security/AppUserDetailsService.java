package com.hbelange.GymProgressTracker.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbelange.GymProgressTracker.entity.User;
import com.hbelange.GymProgressTracker.repository.UserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        // materialize authorities while the persistence context is still open, since
        // User.authorities is lazy and the entity is detached by the time getAuthorities() is called
        List<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(authority -> (GrantedAuthority) new SimpleGrantedAuthority(authority.getAuthority()))
                .toList();
        return new AppUserPrincipal(user, authorities);
    }
}
