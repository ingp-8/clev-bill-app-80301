package com.clevstack.clevbill.security;

import com.clevstack.clevbill.model.Permission;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves a user's authorities fresh on every request from their current
 * roles/permissions — see JwtService for why nothing is cached in the
 * token itself. Two kinds of authority are granted: {@code ROLE_<name>}
 * for coarse role checks, and {@code <module>:<action>} (matching
 * Permission's moduleCode/action exactly, e.g. {@code MASTERS_ITEM:EDIT})
 * per permission any of the user's roles carries — controllers check this
 * form directly via {@code @PreAuthorize("hasAuthority('MODULE:ACTION')")}.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user with username: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities(authoritiesFor(user))
                .build();
    }

    private Set<GrantedAuthority> authoritiesFor(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + sanitize(role.getRoleName())));
            for (Permission permission : role.getPermissions()) {
                authorities.add(
                        new SimpleGrantedAuthority(permission.getModuleCode() + ":" + permission.getAction()));
            }
        }
        return authorities;
    }

    private String sanitize(String roleName) {
        return roleName.trim().toUpperCase().replace(' ', '_');
    }
}
