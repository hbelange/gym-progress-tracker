package com.hbelange.GymProgressTracker.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hbelange.GymProgressTracker.entity.User;

/** UserDetails whose principal name is the immutable user ID, not the mutable display username. */
public class AppUserPrincipal implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserPrincipal(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getId().toString();
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled() == 1;
    }
}
