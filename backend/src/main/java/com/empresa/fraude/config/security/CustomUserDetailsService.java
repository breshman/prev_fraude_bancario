package com.empresa.fraude.config.security;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.UsuarioSistemaEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.UsuarioSistemaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioSistemaJpaRepository usuarioRepo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioSistemaEntity usuario = usuarioRepo.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getCodigoRol()))
                .collect(Collectors.toList());

        boolean enabled = "ACTIVO".equalsIgnoreCase(usuario.getEstadoUsuario());

        return new User(
                usuario.getNombreUsuario(),
                usuario.getHashPassword(),
                enabled,
                true,
                true,
                true,
                authorities
        );
    }
}
