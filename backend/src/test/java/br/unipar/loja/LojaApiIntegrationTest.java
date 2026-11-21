package br.unipar.loja;

import br.unipar.loja.model.*;
import br.unipar.loja.repository.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Testes exercitam a API HTTP, autenticação real, JPA, regras e transações em conjunto. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LojaApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired ProdutoRepository produtos;
    @Autowired PedidoRepository pedidos;
    @Autowired ItemPedidoRepository itens;
    @Autowired PasswordEncoder senhas;
    String admin, comum, outro;
    Categoria categoria;

    @BeforeEach
    void preparar() throws Exception {
        itens.deleteAll(); pedidos.deleteAll(); produtos.deleteAll(); categorias.deleteAll(); usuarios.deleteAll();
        String hash = senhas.encode("Senha123!");
        usuarios.saveAll(List.of(new Usuario("Admin", "admin@teste.com", hash, Perfil.ADMIN),
            new Usuario("Cliente", "cliente@teste.com", hash, Perfil.COMUM),
            new Usuario("Outro", "outro@teste.com", hash, Perfil.COMUM)));
        admin = login("admin@teste.com"); comum = login("cliente@teste.com"); outro = login("outro@teste.com");
        categoria = categorias.save(new Categoria("Papelaria", "Materiais de estudo"));
    }

    @Test
    void exigeLoginERecusaCredenciaisInvalidas() throws Exception {
        mvc.perform(get("/api/produtos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/produtos").header("Authorization", "Bearer invalido")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"cliente@teste.com\",\"senha\":\"errada\"}"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"naoexiste@teste.com\",\"senha\":\"errada\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroNaoAceitaPerfilESenhaNuncaApareceNaResposta() throws Exception {
        String cadastro = "{\"nome\":\"Nova conta\",\"email\":\"nova@teste.com\",\"senha\":\"Segredo123!\"}";
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
            .content(cadastro.substring(0, cadastro.length() - 1) + ",\"perfil\":\"ADMIN\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(cadastro))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.perfil").value("COMUM"))
            .andExpect(jsonPath("$.senhaHash").doesNotExist()).andExpect(jsonPath("$.senha").doesNotExist());
        Usuario usuario = usuarios.findByEmail("nova@teste.com").orElseThrow();
        assertThat(usuario.getSenhaHash()).isNotEqualTo("Segredo123!");
        assertThat(senhas.matches("Segredo123!", usuario.getSenhaHash())).isTrue();
        mvc.perform(get("/api/auth/me").header("Authorization", comum))
            .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("COMUM"))
            .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void usuarioComumNaoPodeAlterarCatalogo() throws Exception {
        Produto produto = produto("Caneta", "Azul", "5.00", 5);
        for (var request : List.of(post("/api/produtos").content(produtoJson("Novo", "Azul", "5.00", 5)),
                put("/api/produtos/" + produto.getId()).content(produtoJson("Outro", "Azul", "5.00", 5)),
                delete("/api/produtos/" + produto.getId()),
                post("/api/categorias").content("{\"nome\":\"Nova\",\"descricao\":\"\"}"),
                put("/api/categorias/" + categoria.getId()).content("{\"nome\":\"Outra\",\"descricao\":\"\"}"),
                delete("/api/categorias/" + categoria.getId()))) {
            mvc.perform(request.header("Authorization", comum).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
        }
        assertThat(produtos.findById(produto.getId()).orElseThrow().getNome()).isEqualTo("Caneta");
    }

    @Test
    void crudCompletoDeCategorias() throws Exception {
        var criada = body(mvc.perform(post("/api/categorias").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Tecnologia\",\"descricao\":\"Eletrônicos\"}"))
            .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn());
        long id = criada.get("id").asLong();
        mvc.perform(get("/api/categorias/" + id).header("Authorization", comum)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Tecnologia"));
        mvc.perform(get("/api/categorias").header("Authorization", comum)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(put("/api/categorias/" + id).header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Informática\",\"descricao\":\"Atualizada\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Informática"));
        mvc.perform(delete("/api/categorias/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/categorias/" + id).header("Authorization", admin)).andExpect(status().isNotFound());
    }

    @Test
    void crudCompletoDeProdutosEPersistenciaNoRepositorio() throws Exception {
        var criado = body(mvc.perform(post("/api/produtos").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content(produtoJson("Caneta", "Azul", "7.90", 10))).andExpect(status().isCreated()).andReturn());
        long id = criado.get("id").asLong();
        assertThat(produtos.findById(id).orElseThrow().getPreco()).isEqualByComparingTo("7.90");
        mvc.perform(get("/api/produtos/" + id).header("Authorization", comum)).andExpect(status().isOk())
            .andExpect(jsonPath("$.categoriaNome").value("Papelaria")).andExpect(jsonPath("$.categoriaId").value(categoria.getId()))
            .andExpect(jsonPath("$.categoria").doesNotExist());
        mvc.perform(put("/api/produtos/" + id).header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content(produtoJson("Caneta gel", "Preta", "9.50", 7)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Caneta gel"));
        assertThat(produtos.findById(id).orElseThrow().getEstoque()).isEqualTo(7);
        mvc.perform(delete("/api/produtos/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/produtos/" + id).header("Authorization", comum)).andExpect(status().isNotFound());
    }

    @Test
    void filtrosNoPathEComMultiplosParametros() throws Exception {
        Produto desejado = produto("Caneta gel", "Azul", "7.90", 10);
        produto("Caneta", "Preta", "5.00", 10);
        produto("Caderno", "Azul", "20.00", 10);
        Categoria outra = categorias.save(new Categoria("Escritório", ""));
        produtos.save(new Produto("Mesa", "", "Branca", new BigDecimal("100.00"), 2, true, outra));
        mvc.perform(get("/api/categorias/" + categoria.getId() + "/produtos").header("Authorization", comum))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/produtos").param("nome", "CANETA").param("cor", "azul").param("precoMax", "10").header("Authorization", comum))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(desejado.getId()));
        mvc.perform(get("/api/produtos").param("precoMin", "20").param("precoMax", "5").header("Authorization", comum)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/produtos").param("precoMin", "-1").header("Authorization", comum)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/categorias/999999/produtos").header("Authorization", comum)).andExpect(status().isNotFound());
    }

    @Test
    void validacaoEDuplicidadeDeCategoria() throws Exception {
        mvc.perform(post("/api/produtos").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content(produtoJson("Inválido", "Azul", "-1.00", -1))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/produtos").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content(produtoJson("Inválido", "Azul", "1.001", 5))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/categorias").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\" PAPELARIA \",\"descricao\":\"\"}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/categorias").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"   \",\"descricao\":\"\"}"))
            .andExpect(status().isBadRequest());
        produto("Caneta", "Azul", "5.00", 5);
        mvc.perform(delete("/api/categorias/" + categoria.getId()).header("Authorization", admin)).andExpect(status().isConflict());
    }

    @Test
    void pedidoCalculaTotalBaixaEstoqueEPreservaPrecoHistorico() throws Exception {
        Produto p = produto("Caneta", "Azul", "7.90", 10);
        Produto q = produto("Caderno", "Verde", "32.50", 4);
        var pedido = body(mvc.perform(post("/api/pedidos").header("Authorization", comum).contentType(MediaType.APPLICATION_JSON)
            .content(pedidoJson(p, 2, q, 1))).andExpect(status().isCreated()).andReturn());
        assertThat(pedido.get("total").decimalValue()).isEqualByComparingTo("48.30");
        assertThat(pedido.get("itens").size()).isEqualTo(2);
        assertThat(produtos.findById(p.getId()).orElseThrow().getEstoque()).isEqualTo(8);
        assertThat(produtos.findById(q.getId()).orElseThrow().getEstoque()).isEqualTo(3);
        mvc.perform(put("/api/produtos/" + p.getId()).header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content(produtoJson("Caneta nova", "Azul", "99.00", 8))).andExpect(status().isOk());
        mvc.perform(get("/api/pedidos/" + pedido.get("id").asLong()).header("Authorization", comum))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(48.30))
            .andExpect(jsonPath("$.itens[0].nomeProduto").value("Caneta"))
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(7.90));
        mvc.perform(delete("/api/produtos/" + p.getId()).header("Authorization", admin)).andExpect(status().isConflict());
    }

    @Test
    void falhaNoSegundoItemDesfazPrimeiraBaixa() throws Exception {
        Produto p = produto("Caneta", "Azul", "7.90", 10);
        Produto q = produto("Caderno", "Verde", "32.50", 0);
        mvc.perform(post("/api/pedidos").header("Authorization", comum).contentType(MediaType.APPLICATION_JSON)
            .content(pedidoJson(p, 2, q, 1))).andExpect(status().isConflict());
        assertThat(produtos.findById(p.getId()).orElseThrow().getEstoque()).isEqualTo(10);
        assertThat(pedidos.count()).isZero();
        assertThat(itens.count()).isZero();
    }

    @Test
    void rejeitaPedidoVazioDuplicadoQuantidadeInvalidaETotalEnviado() throws Exception {
        Produto p = produto("Caneta", "Azul", "7.90", 10);
        for (String conteudo : List.of("{\"itens\":[]}", pedidoJson(p, 0), pedidoJson(p, -1), pedidoJson(p, 1, p, 1),
                "{\"itens\":[null]}", pedidoJson(p, 1).replace("{\"itens\"", "{\"total\":0,\"itens\""))) {
            mvc.perform(post("/api/pedidos").header("Authorization", comum).contentType(MediaType.APPLICATION_JSON).content(conteudo)).andExpect(status().isBadRequest());
        }
        assertThat(pedidos.count()).isZero();
        assertThat(produtos.findById(p.getId()).orElseThrow().getEstoque()).isEqualTo(10);
    }

    @Test
    void clienteNaoVeNemCancelaPedidoDeOutroEAdminVeTodos() throws Exception {
        Produto p = produto("Caneta", "Azul", "7.90", 10);
        var pedido = body(mvc.perform(post("/api/pedidos").header("Authorization", comum).contentType(MediaType.APPLICATION_JSON)
            .content(pedidoJson(p, 2))).andExpect(status().isCreated()).andReturn());
        long id = pedido.get("id").asLong();
        mvc.perform(get("/api/pedidos").header("Authorization", outro)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/pedidos/" + id).header("Authorization", outro)).andExpect(status().isForbidden());
        mvc.perform(patch("/api/pedidos/" + id + "/cancelar").header("Authorization", outro)).andExpect(status().isForbidden());
        mvc.perform(get("/api/pedidos").header("Authorization", admin)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/pedidos/" + id).header("Authorization", admin)).andExpect(status().isOk());
        assertThat(produtos.findById(p.getId()).orElseThrow().getEstoque()).isEqualTo(8);
    }

    @Test
    void cancelamentoRestauraEstoqueUmaUnicaVez() throws Exception {
        Produto p = produto("Caneta", "Azul", "7.90", 10);
        long id = body(mvc.perform(post("/api/pedidos").header("Authorization", comum).contentType(MediaType.APPLICATION_JSON)
            .content(pedidoJson(p, 2))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        mvc.perform(patch("/api/pedidos/" + id + "/cancelar").header("Authorization", comum))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(patch("/api/pedidos/" + id + "/cancelar").header("Authorization", comum)).andExpect(status().isConflict());
        assertThat(produtos.findById(p.getId()).orElseThrow().getEstoque()).isEqualTo(10);
    }

    @Test
    void duasComprasConcorrentesNaoVendemAmesmaUnidade() throws Exception {
        Produto p = produto("Última caneta", "Azul", "7.90", 1);
        var inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> compra = () -> {
                inicio.await();
                return mvc.perform(post("/api/pedidos").header("Authorization", comum).contentType(MediaType.APPLICATION_JSON)
                    .content(pedidoJson(p, 1))).andReturn().getResponse().getStatus();
            };
            Future<Integer> primeira = executor.submit(compra), segunda = executor.submit(compra);
            inicio.countDown();
            assertThat(List.of(primeira.get(15, TimeUnit.SECONDS), segunda.get(15, TimeUnit.SECONDS))).containsExactlyInAnyOrder(201, 409);
            assertThat(produtos.findById(p.getId()).orElseThrow().getEstoque()).isZero();
            assertThat(pedidos.count()).isEqualTo(1);
        } finally { executor.shutdownNow(); }
    }

    @Test
    void swaggerExplicaDTOsERelacionamentosSemExporSenha() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/categorias/{id}/produtos']").exists())
            .andExpect(jsonPath("$.components.schemas.ProdutoRequest").exists())
            .andExpect(jsonPath("$.components.schemas.ProdutoResponse").exists())
            .andExpect(jsonPath("$.components.schemas.Usuario").doesNotExist());
    }

    private String login(String email) throws Exception {
        JsonNode resultado = body(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("email", email, "senha", "Senha123!")))).andExpect(status().isOk()).andReturn());
        return "Bearer " + resultado.get("token").asText();
    }
    private Produto produto(String nome, String cor, String preco, int estoque) {
        return produtos.save(new Produto(nome, "Descrição", cor, new BigDecimal(preco), estoque, true, categoria));
    }
    private String produtoJson(String nome, String cor, String preco, int estoque) throws Exception {
        return json.writeValueAsString(Map.of("nome", nome, "descricao", "Descrição", "cor", cor, "preco", new BigDecimal(preco),
            "estoque", estoque, "ativo", true, "categoriaId", categoria.getId()));
    }
    private String pedidoJson(Produto p, int quantidade) throws Exception {
        return json.writeValueAsString(Map.of("itens", List.of(Map.of("produtoId", p.getId(), "quantidade", quantidade))));
    }
    private String pedidoJson(Produto p, int quantidadeP, Produto q, int quantidadeQ) throws Exception {
        return json.writeValueAsString(Map.of("itens", List.of(Map.of("produtoId", p.getId(), "quantidade", quantidadeP), Map.of("produtoId", q.getId(), "quantidade", quantidadeQ))));
    }
    private JsonNode body(MvcResult result) throws Exception { return json.readTree(result.getResponse().getContentAsByteArray()); }
}
