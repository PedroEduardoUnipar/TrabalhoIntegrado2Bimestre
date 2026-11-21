# Roteiro de apresentação — até 5 minutos

Meta: **4 minutos e 50 segundos**, com a aplicação e o Swagger abertos antes de começar a gravação.

## Preparação

1. Inicie o back-end e o front e confirme as contas de demonstração.
2. Abra o projeto na IDE, o Swagger e o front em abas diferentes.
3. Deixe fáceis de abrir: `ProdutoController`, `ProdutoService`, `ProdutoRepository`, `ProdutoRequest`, `ProdutoResponse`, `Categoria`, `Pedido` e `SecurityConfig`.
4. Defina os integrantes do grupo e confira que o link do repositório está acessível ao professor.
5. Use um produto de demonstração e cadastre um produto temporário para demonstrar a exclusão sem vínculo com pedidos.

## Tempo e conteúdo

| Tempo | O que mostrar | Sugestão de fala |
| --- | --- | --- |
| 0:00–0:20 | Tela inicial e nomes | “Este é o nosso trabalho integrado do segundo bimestre. Escolhemos o tema loja e integramos uma API REST em Spring Boot com um front em Angular.” |
| 0:20–1:10 | Pacotes, records, entidades | “Organizamos o back-end em Controller, Service e Repository. O Controller trata HTTP, o Service concentra as regras e o Repository usa Spring Data JPA. Os DTOs de entrada e saída são records. Aqui estão os relacionamentos de categoria com produtos e pedido com itens.” |
| 1:10–2:00 | Login no Swagger, Authorize, CRUD | “O login gera um token JWT. Com o perfil administrador, podemos criar, listar, buscar pelo ID, atualizar e excluir produtos. As entidades não são usadas como corpo das requisições ou respostas.” |
| 2:00–2:35 | Dois filtros no Swagger | “Esta consulta recebe o ID da categoria no path. Esta outra combina nome e cor nos parâmetros da URL. O resultado considera os dois filtros juntos.” |
| 2:35–3:00 | Conta comum e 403 | “O perfil comum pode consultar o catálogo e fazer pedidos. Ao tentar alterar o catálogo, a própria API bloqueia a operação com 403. A proteção está no servidor, além da navegação do front.” |
| 3:00–4:15 | Front: catálogo, filtro, carrinho e pedido | “O Angular usa a API real. Podemos filtrar os produtos, adicionar ao carrinho e finalizar. O servidor calcula o total e baixa o estoque. O cliente consulta seus próprios pedidos. No cancelamento, o estoque é devolvido uma única vez.” |
| 4:15–4:40 | Banco/arquivo e testes | “Os dados são persistidos no banco. O modo local usa H2 em arquivo e também disponibilizamos configuração para PostgreSQL. Os testes verificam autorização, CRUD, filtros, rollback e compra concorrente da última unidade.” |
| 4:40–4:50 | Git e encerramento | “O código, as instruções, a coleção Postman e os testes estão neste repositório. Obrigado.” |

## Divisão opcional para três integrantes

| Integrante | Trecho |
| --- | --- |
| Pessoa 1 | Objetivo, organização do código, DTOs e relacionamentos |
| Pessoa 2 | Swagger, CRUD, filtros e autorização |
| Pessoa 3 | Front, pedido/cancelamento, persistência e link do Git |

Substitua “Pessoa 1/2/3” pelos nomes reais. A apresentação também pode ser feita por uma ou duas pessoas. Grave a tela e a narração; não é necessário mostrar cada linha de código.
