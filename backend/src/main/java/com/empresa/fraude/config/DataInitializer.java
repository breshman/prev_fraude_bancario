package com.empresa.fraude.config;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.UsuarioSistemaEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.UsuarioSistemaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioSistemaJpaRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Garantizar que los usuarios de prueba tengan contraseñas BCrypt válidas
        usuarioRepo.findByNombreUsuario("analista_seguridad").ifPresent(u -> {
            u.setHashPassword(passwordEncoder.encode("Admin1234#"));
            usuarioRepo.save(u);
        });

        usuarioRepo.findByNombreUsuario("admin_fraude").ifPresent(u -> {
            u.setHashPassword(passwordEncoder.encode("Admin1234#"));
            usuarioRepo.save(u);
        });
    }
}
