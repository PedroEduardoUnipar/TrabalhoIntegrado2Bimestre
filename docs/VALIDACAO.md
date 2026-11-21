# Validação da entrega

Validação realizada em **21/11/2026**, com Java 21 e Node.js 24.

| Verificação | Resultado |
| --- | --- |
| Compilação e empacotamento do back-end | Aprovado; JAR executável gerado |
| Testes de integração da API com H2 em memória | **14 testes aprovados, zero falhas/erros** |
| Build de produção do Angular | Aprovado |
| Fluxo de login/perfis no navegador | Aprovado em desktop e celular |
| CRUD de categorias/produtos pelo front | Aprovado em desktop e celular |
| Cadastro, filtro no path, compra e cancelamento pelo front | Aprovado em desktop e celular |
| Total de cenários E2E | **6 cenários aprovados** |
| Persistência em H2 em arquivo após reiniciar o processo Java | Aprovado; nome, preço e estoque preservados |
| Coleção Postman | JSON e corpos das 26 requisições validados |

## O que os testes da API cobrem

1. Falta de autenticação, token inválido e credenciais incorretas.
2. Cadastro público COMUM, rejeição de perfil enviado no request e ausência de senha hash nos responses.
3. Bloqueio das seis operações de escrita do catálogo para usuário comum.
4. CRUD completo de categorias.
5. CRUD completo de produtos, consulta por ID e persistência via JPA.
6. Filtro no path e combinação de nome/cor/preço nos parâmetros.
7. Validação de preço/estoque, categoria duplicada e bloqueio de exclusão com vínculo.
8. Cálculo do total, baixa de estoque e preservação do nome/preço histórico do pedido.
9. Rollback integral quando um dos itens não possui estoque.
10. Rejeição de pedido vazio, item nulo, quantidade inválida, produto repetido e total enviado pelo cliente.
11. Isolamento de pedidos entre clientes e acesso de administrador.
12. Devolução do estoque uma única vez no cancelamento.
13. Duas compras simultâneas da última unidade: uma aprovada e outra bloqueada, sem estoque negativo.
14. Geração do OpenAPI com DTOs e rota de filtro por categoria.

## Condições e limites

- Os testes foram executados com H2 em memória e H2 em arquivo. A configuração PostgreSQL foi incluída, mas não foi executada nesta validação.
- O Docker Compose não foi executado neste ambiente.
- Os testes E2E usaram Chromium Headless, em tamanhos de tela **1440 × 1000** e **390 × 844**, com os três fluxos executados separadamente em cada tamanho.
- A coleção Postman foi validada como arquivo JSON; não foi executada pelo aplicativo Postman. Os endpoints correspondentes foram cobertos pela API e pelos fluxos E2E.
- A gravação do vídeo e o preenchimento dos integrantes dependem do grupo. O roteiro está em `ROTEIRO_VIDEO.md`.
