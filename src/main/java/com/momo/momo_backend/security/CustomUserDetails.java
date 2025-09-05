package com.momo.momo_backend.security;

import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.entity.UserCredential;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final transient User user;
    private final transient UserCredential credential;

    public CustomUserDetails(User user, UserCredential credential) {
        this.user = user;
        this.credential = credential;
    }

    public User getUser() {
        return this.user;
    }

    @Override
    public String getUsername() {
        return credential.getLoginId();
    }

    @Override
    public String getPassword() {
        return credential.getPw();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 최소 USER 권한 부여
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}