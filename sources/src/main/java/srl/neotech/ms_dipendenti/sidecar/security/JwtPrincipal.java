package srl.neotech.ms_dipendenti.sidecar.security;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Principal derivato dal JWT, con nome utente e ruoli per la sicurezza basata su ruoli.
 */
public class JwtPrincipal implements Principal {

    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtPrincipal(String name, Collection<String> roles) {
        this.name = name;
        this.authorities = roles == null ? Collections.emptyList()
                : roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return name;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
