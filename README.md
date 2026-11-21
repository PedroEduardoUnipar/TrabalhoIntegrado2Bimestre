# Loja Integrada — 2º bimestre

Aplicação de loja com API REST em Spring Boot e front-end em Angular. Permite login, cadastro de usuários, gestão de categorias/produtos e realização/cancelamento de pedidos. O administrador gerencia o catálogo; o usuário comum pode consultar produtos e acompanhar seus próprios pedidos.

**Repositório:** https://github.com/PedroEduardoUnipar/TrabalhoIntegrado2Bimestre

**Disciplina:** Trabalho integrado de Desenvolvimento Back-end e Front-end — ADS / UNIPAR.

## Executar a demonstração

Pré-requisitos: **JDK 21** (Java completo, com compilador) e **Node.js 24**. O Maven Wrapper está incluído; não precisa instalar o Maven separadamente. É necessária conexão com a internet na primeira execução para baixar as dependências.

Abra dois terminais na pasta do projeto.

**Terminal 1 — back-end:**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

No Linux/macOS, use `bash mvnw spring-boot:run`.

**Terminal 2 — front-end:**

```powershell
cd frontend
npm ci
npm start
```

No Windows, também é possível executar `iniciar-demo.cmd` na raiz do projeto, após instalar os pré-requisitos. Aguarde os dois serviços iniciarem.

| Acesso | Endereço |
| --- | --- |
| Front-end | http://localhost:4200 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| API | http://localhost:8080/api |

Contas disponíveis **somente no perfil `demo`**, que é o padrão local:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Administrador | admin@loja.com | Admin123! |
| Comum | cliente@loja.com | Cliente123! |

O modo demonstração usa **H2 em arquivo**, em `backend/data/loja.mv.db`. Os dados continuam salvos após parar e reiniciar o back-end. Quando o catálogo estiver vazio, serão criadas três categorias e seis produtos de exemplo. Os testes automatizados usam outro banco.

## Atendimento aos requisitos

| Requisito | Implementação / onde conferir |
| --- | --- |
| API REST | Controllers em `backend/src/main/java/br/unipar/loja/controller` |
| Persistência com Spring Data JPA | Interfaces `JpaRepository`, entidades JPA e banco relacional |
| Repository, Service e Controller | Pacotes separados; validações de negócio, autorização por proprietário e transações nos Services |
| DTOs de entrada e saída em `record` | Pacote `dto`; entidades nunca são retornadas ou recebidas no corpo HTTP |
| CRUD completo | Categorias **e** produtos: POST, GET lista, GET por ID, PUT e DELETE |
| Relacionamento 1:N | Categoria → produtos; pedido → itens; usuário → pedidos pelo lado `@ManyToOne` de Pedido |
| Filtro recebido no path | `GET /api/categorias/{id}/produtos` |
| Vários parâmetros de consulta | `GET /api/produtos?nome=caneta&cor=azul` |
| Login | `POST /api/auth/login`, senha BCrypt e token JWT com expiração |
| Extra: autorização por perfil | ADMIN / COMUM; escrita do catálogo protegida no servidor |
| Integração com front | Angular consome a API real, incluindo filtros, CRUD e pedidos |
| Entrega em Git | Código, instruções, testes, coleção Postman e roteiro do vídeo neste repositório |

## Organização

```text
backend/src/main/java/br/unipar/loja/
  controller/    Recebe HTTP, valida requests e define respostas
  service/       Regras de negócio e limites das transações
  repository/    Acesso ao banco com Spring Data JPA
  model/         Entidades e relacionamentos JPA
  dto/           Records de entrada e saída e conversão explícita
  security/      Autenticação, autorização e emissão/validação de JWT
  exception/     Respostas de erro padronizadas
  config/        Swagger e inicialização de demonstração
frontend/src/app/
  core/          Cliente HTTP, autenticação, carrinho e guards
  pages/         Login, catálogo, cadastros, carrinho e pedidos
  shared/        Modal acessível com dialog nativo
docs/            Modelo, API, roteiro e coleção Postman
```

Tecnologias: Java 21, Spring Boot 3.5.16, Spring Data JPA, Spring Security, Bean Validation, Springdoc/Swagger, PostgreSQL, H2 e Angular 21. Os valores monetários e totais do servidor usam `BigDecimal`.

## Perfis e regras de negócio

| Operação | Comum | Administrador |
| --- | --- | --- |
| Consultar categorias/produtos | Sim | Sim |
| Criar, editar e excluir categorias/produtos | Não | Sim |
| Criar um pedido | Sim | Sim |
| Consultar e cancelar os próprios pedidos | Sim | Sim |
| Consultar e cancelar pedidos de outros usuários | Não | Sim |

- O cadastro público sempre cria perfil `COMUM`. O request de cadastro não aceita perfil; as respostas informam o perfil, mas nunca incluem a senha ou seu hash.
- Cada produto pertence a uma categoria. Uma categoria pode começar sem produtos; uma categoria com produtos vinculados não pode ser excluída.
- Preço deve ser positivo, com até duas casas decimais e no máximo R$ 9.999.999,99. Estoque informado no cadastro deve ser inteiro entre 0 e 1.000.000.
- O pedido aceita de 1 a 50 produtos diferentes, com quantidade de 1 a 9.999 por produto. Produto inativo ou sem estoque suficiente impede a compra inteira.
- O cliente envia somente os IDs e as quantidades. O servidor identifica o usuário pelo token, consulta os preços e calcula subtotais/total.
- A criação do pedido e as baixas de estoque são atômicas. Bloqueios no banco protegem a última unidade contra duas compras simultâneas.
- Cada item preserva nome e preço do momento da compra. Alterar o produto não muda o pedido anterior.
- Produtos presentes em pedidos não podem ser excluídos; podem ser desativados.
- O cancelamento é permitido uma única vez e devolve o estoque dentro da mesma transação.
- A saída da conta remove a sessão do navegador. O JWT tem validade de duas horas; o projeto não implementa lista de revogação nem renovação de tokens.

Pedidos têm criação, listagem, consulta por ID e cancelamento. O CRUD completo exigido está em **categorias e produtos**; pedidos preservam o histórico e não possuem edição/exclusão.

## PostgreSQL

O perfil `postgres` usa o mesmo código de negócio e não carrega as contas/produtos públicos de demonstração. Configure `JWT_SECRET` com pelo menos 32 bytes e escolha as credenciais do banco e do primeiro administrador.

**Opção A — aplicação completa com Docker Compose:**

```powershell
Copy-Item .env.example .env
# Edite .env e escolha seus valores.
docker compose up --build
```

No Linux/macOS, use `cp .env.example .env`. Os endereços do front e do Swagger continuam os mesmos. O volume `loja_db` mantém os dados do PostgreSQL. O catálogo começa vazio: entre como administrador e cadastre categorias e produtos. As imagens Docker/Compose são uma alternativa de execução; confira `docs/VALIDACAO.md` para o que foi efetivamente testado.

**Opção B — PostgreSQL já instalado e aplicação pela IDE/terminal:**

Crie um banco chamado `loja_integrada` e ajuste as variáveis no terminal do back-end:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/loja_integrada"
$env:DB_USER="postgres"
$env:DB_PASSWORD="sua-senha"
$env:JWT_SECRET="uma-chave-propria-com-pelo-menos-32-bytes"
$env:APP_ADMIN_EMAIL="admin@loja.com"
$env:APP_ADMIN_PASSWORD="SuaSenhaAdmin123!"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Em uma IDE, defina o perfil ativo `postgres` e essas variáveis na configuração de execução. `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD` criam um administrador **apenas se o e-mail ainda não existir**; não mudam o perfil nem a senha de uma conta existente. O exemplo usa `ddl-auto=update` para o ambiente acadêmico.

## Conferir no Swagger/Postman

1. Faça `POST /api/auth/login` com uma das contas de demonstração.
2. Copie o campo `token` do response. No Swagger, clique em **Authorize** e cole somente o token.
3. Execute o CRUD de categorias/produtos.
4. Compare `/api/categorias/1/produtos` e `/api/produtos?nome=caneta&cor=azul`. Use o ID real da categoria caso já tenha alterado os dados.
5. Faça login como usuário comum e tente criar um produto: o servidor retorna **403**.
6. Crie um pedido e consulte seu total e o estoque. Cancele e verifique a reposição.

Também é possível importar [`docs/Loja_Integrada.postman_collection.json`](docs/Loja_Integrada.postman_collection.json). A coleção salva o token do login e os IDs criados automaticamente.

## Testes

**Back-end:**

```powershell
cd backend
.\mvnw.cmd verify
```

Os testes exercitam HTTP, autenticação, banco, filtros, CRUD, propriedade dos pedidos, validações, rollback e concorrência de estoque. Por padrão usam H2 em memória, sem alterar os dados da demonstração.

**Build do front:**

```powershell
cd frontend
npm ci
npm run build
```

**Fluxo completo no navegador (desktop e celular):**

Inicie o back-end com um banco de demonstração separado e inicie o front normalmente:

```powershell
# Terminal do back-end
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:h2:file:./data/loja-e2e;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
```

Depois, no terminal do front:

```powershell
npx playwright install chromium
npm run test:e2e
```

Os dois serviços devem estar rodando em 8080 e 4200. Os testes criam dados próprios, verificam login/perfil, CRUD, filtros, cadastro, compra e cancelamento. Não rode os testes E2E em um banco com dados reais.

## Entrega e apresentação

- Link do Git: https://github.com/PedroEduardoUnipar/TrabalhoIntegrado2Bimestre
- Vídeo de até 5 minutos: seguir [`docs/ROTEIRO_VIDEO.md`](docs/ROTEIRO_VIDEO.md).
- Integrantes: preencher os nomes do grupo, com no máximo três pessoas, antes de entregar.
- Modelo e decisões: [`docs/MODELO_E_API.md`](docs/MODELO_E_API.md).
- Resultado da validação: [`docs/VALIDACAO.md`](docs/VALIDACAO.md).

## Referências técnicas

- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Spring Security — JWT](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html)
- [Springdoc — compatibilidade](https://springdoc.org/v2/faq.html)
- [Angular — versões compatíveis](https://angular.dev/reference/versions)
