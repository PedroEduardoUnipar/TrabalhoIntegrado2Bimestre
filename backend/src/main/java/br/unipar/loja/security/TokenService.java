package br.unipar.loja.security;

import br.unipar.loja.dto.*;
import br.unipar.loja.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.*;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final String emissor;
    private final Duration duracao;
    public TokenService(JwtEncoder encoder, @Value("${app.jwt.issuer}") String emissor,
                        @Value("${app.jwt.duration}") Duration duracao) {
        this.encoder = encoder;
        this.emissor = emissor;
        this.duracao = duracao;
    }
    public LoginResponse gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(duracao);
        var claims = JwtClaimsSet.builder().issuer(emissor).issuedAt(agora).expiresAt(expiracao)
            .subject(usuario.getId().toString()).claim("perfil", usuario.getPerfil().name()).build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", expiracao, DtoMapper.usuario(usuario));
    }
}
