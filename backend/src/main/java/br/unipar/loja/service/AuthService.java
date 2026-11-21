package br.unipar.loja.service;

import br.unipar.loja.dto.*;
import br.unipar.loja.model.*;
import br.unipar.loja.repository.UsuarioRepository;
import br.unipar.loja.security.TokenService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import static br.unipar.loja.exception.RegraNegocioException.*;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final AuthenticationManager autenticacao;
    private final TokenService tokens;
    public AuthService(UsuarioRepository usuarios, PasswordEncoder senhas, AuthenticationManager autenticacao, TokenService tokens) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.autenticacao = autenticacao;
        this.tokens = tokens;
    }
    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest request) {
        validarSenha(request.senha());
        String email = normalizarEmail(request.email());
        if (usuarios.existsByEmail(email)) throw conflito("Já existe uma conta com este e-mail.");
        // O perfil é definido no servidor. Cadastro público sempre cria usuário COMUM.
        return DtoMapper.usuario(usuarios.save(new Usuario(request.nome().strip(), email,
            senhas.encode(request.senha()), Perfil.COMUM)));
    }
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        validarSenha(request.senha());
        String email = normalizarEmail(request.email());
        autenticacao.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, request.senha()));
        Usuario usuario = usuarios.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
        return tokens.gerar(usuario);
    }
    @Transactional(readOnly = true)
    public UsuarioResponse atual(Jwt jwt) {
        return DtoMapper.usuario(usuarios.findById(usuarioId(jwt)).orElseThrow(() -> new BadCredentialsException("Usuário inválido.")));
    }
    public Long usuarioId(Jwt jwt) {
        try { return Long.valueOf(jwt.getSubject()); }
        catch (NumberFormatException e) { throw new BadCredentialsException("Token inválido."); }
    }
    private void validarSenha(String senha) {
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) throw invalido("A senha deve ocupar no máximo 72 bytes em UTF-8.");
    }
    private String normalizarEmail(String email) { return email.strip().toLowerCase(Locale.ROOT); }
}
