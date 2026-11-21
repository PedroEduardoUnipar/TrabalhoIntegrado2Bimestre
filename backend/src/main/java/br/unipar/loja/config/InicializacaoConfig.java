package br.unipar.loja.config;

import br.unipar.loja.model.*;
import br.unipar.loja.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.Locale;

@Configuration
public class InicializacaoConfig {
    @Bean
    CommandLineRunner administrador(UsuarioRepository usuarios, PasswordEncoder senhas,
            @Value("${app.admin.email}") String email, @Value("${app.admin.password}") String senha) {
        return args -> {
            if (!email.isBlank() && !senha.isBlank()) {
                if (senha.length() < 8 || senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
                    throw new IllegalStateException("APP_ADMIN_PASSWORD deve ter pelo menos 8 caracteres e no máximo 72 bytes UTF-8.");
                }
                String normalizado = email.strip().toLowerCase(Locale.ROOT);
                if (!usuarios.existsByEmail(normalizado)) usuarios.save(new Usuario("Administrador", normalizado, senhas.encode(senha), Perfil.ADMIN));
            }
        };
    }

    @Bean
    @Profile("demo")
    CommandLineRunner demonstracao(UsuarioRepository usuarios, CategoriaRepository categorias,
                                  ProdutoRepository produtos, PasswordEncoder senhas) {
        return args -> {
            if (!usuarios.existsByEmail("admin@loja.com")) usuarios.save(new Usuario("Administrador", "admin@loja.com", senhas.encode("Admin123!"), Perfil.ADMIN));
            if (!usuarios.existsByEmail("cliente@loja.com")) usuarios.save(new Usuario("Cliente Demo", "cliente@loja.com", senhas.encode("Cliente123!"), Perfil.COMUM));
            // Preenche o catálogo de demonstração apenas quando categorias e produtos estão vazios.
            if (categorias.count() == 0 && produtos.count() == 0) {
                Categoria papelaria = categorias.save(new Categoria("Papelaria", "Ideias que começam no papel."));
                Categoria tecnologia = categorias.save(new Categoria("Tecnologia", "Praticidade para o seu dia."));
                Categoria escritorio = categorias.save(new Categoria("Escritório", "Seu espaço, do seu jeito."));
                produtos.save(new Produto("Caneta gel", "Escrita suave com ponta fina de 0,7 mm.", "Azul", new BigDecimal("7.90"), 45, true, papelaria));
                produtos.save(new Produto("Caderno pontilhado", "Capa resistente, 80 folhas e muitas possibilidades.", "Verde", new BigDecimal("32.50"), 18, true, papelaria));
                produtos.save(new Produto("Caneta esferográfica", "O clássico para acompanhar suas anotações.", "Preta", new BigDecimal("3.90"), 60, true, papelaria));
                produtos.save(new Produto("Mouse sem fio", "Conexão estável e formato confortável.", "Preta", new BigDecimal("89.90"), 12, true, tecnologia));
                produtos.save(new Produto("Teclado compacto", "Mais espaço para trabalhar, estudar e criar.", "Branca", new BigDecimal("149.90"), 8, true, tecnologia));
                produtos.save(new Produto("Organizador de mesa", "Tudo em seu lugar para uma rotina mais leve.", "Azul", new BigDecimal("49.00"), 15, true, escritorio));
            }
        };
    }
}
