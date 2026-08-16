package com.hbelange.GymProgressTracker.security;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hbelange.GymProgressTracker.entity.User;

public class SecurityUser implements UserDetails {

    private final User user;
    private final List<GrantedAuthority> authorities;

    public SecurityUser(User user) {
        this.user = user;
        this.authorities = user.getAuthorities()
            .stream()
            .map(authority -> (GrantedAuthority) new SimpleGrantedAuthority(authority.getAuthority()))
            .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled() == 1;
    }

}
