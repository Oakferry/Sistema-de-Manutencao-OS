# Sistema de Gestão de Ordens de Serviço e Manutenção de Equipamentos

Projeto acadêmico da disciplina de Arquitetura de Software. O sistema registra problemas em equipamentos, organiza o atendimento por técnico e acompanha as ordens de serviço até o encerramento. A versão 2 adiciona a interface web local e a fila persistente por técnico, mantendo o menu de terminal (CLI).

## Funcionalidades

- **Equipamentos:** cadastro, consulta, edição, ativação e inativação. A exclusão é bloqueada quando há histórico de OS.
- **Usuários e técnicos:** cadastro e edição de usuários, perfis de gestor e técnico, vínculo técnico e controle de disponibilidade.
- **Ordens de serviço:** abertura com criticidade, atribuição e reatribuição, diagnóstico, execução, intervenções, materiais, espera por peça, finalização, aprovação e cancelamento.
- **Fila por técnico:** se já houver uma OS ativa, a nova ordem aguarda na fila. Ao liberar o atendimento, o sistema encaminha a próxima ordem elegível, seguindo a ordem de entrada.

O painel web reúne **Ordens**, **Fila dos técnicos** e **Cadastros**. Os formulários mostram os registros disponíveis pelo nome.

## Executar em outro computador com NetBeans

### 1. Preparar o ambiente e abrir o projeto

1. Instale um **JDK 24** e uma instalação do **Apache NetBeans** com suporte a Java e Maven. O projeto usa a configuração `maven.compiler.release=24`.
2. Baixe o código completo da versão desejada no GitHub e extraia o ZIP, ou clone o [repositório](https://github.com/Oakferry/Sistema-de-Manutencao-OS). Use o código-fonte, que inclui o `pom.xml`, a pasta `scripts` e a pasta `src`.
3. No NetBeans, clique em **File → Open Project** e selecione a pasta que contém **`pom.xml`**. Não é necessário criar outro projeto.
4. Se o JDK 24 não estiver configurado, adicione-o em **Tools → Java Platforms** e confira se o Maven está usando esse JDK. No terminal, quando usar Maven externo, confira com `mvn -version`.
5. No painel **Projects**, clique com o botão direito em **sistema-manutencao → Clean and Build**. A primeira compilação pode precisar de internet para baixar as dependências Maven.

### 2. Inicializar e executar a interface web

1. Em **Projects → Source Packages → br.com.manutencao.presentation.web**, clique com o botão direito em **`SistemaManutencaoWeb.java` → Run File**.
2. Aguarde a mensagem **Servidor iniciado** no painel **Output**, na parte de baixo do NetBeans.
3. Abra [http://127.0.0.1:8080/](http://127.0.0.1:8080/). Deixe o processo Java rodando enquanto usa o navegador.

Na primeira execução, o aplicativo lê `scripts/schema.sql` e inicializa o banco SQLite em `data/manutencao.db`, relativo à raiz do projeto. Não é necessário instalar um servidor de banco ou copiar o banco de outra máquina. Para conservar os dados locais, mantenha esse arquivo. Para rodar no terminal, inicie sempre na pasta que contém `pom.xml` e `scripts/`.

### 3. Criar os primeiros registros

Se o banco estiver vazio:

1. Em **Cadastros → Usuários → Novo usuário**, crie um gestor, por exemplo **Mariana Ribeiro** (login `mariana.ribeiro`, perfil **Gestor**).
2. Crie outro usuário com perfil **Técnico**, por exemplo **Rafael Almeida** (login `rafael.almeida`). Em **Cadastros → Técnicos → Vincular técnico**, selecione Rafael.
3. Em **Cadastros → Equipamentos → Novo equipamento**, registre **Quadro de distribuição QD-01**, tipo **Quadro elétrico**, identificador `QD-01` e uma descrição.
4. Em **Ordens → Abrir nova ordem**, selecione o equipamento, o solicitante e a criticidade. Descreva o problema e salve.

### Executar a CLI

No NetBeans, abra **Projects → Source Packages → br.com.manutencao.presentation.cli** e execute **`SistemaManutencao.java`** com **Run File**. Use o menu no painel **Output**. A CLI e a web usam o mesmo SQLite quando iniciadas da mesma raiz do projeto.

### Problemas comuns

| Sintoma | O que conferir |
| --- | --- |
| `Address already in use` | Pare a execução web anterior no **Output** antes de executar novamente. |
| `Arquivo não encontrado: /web/cadastros.js` | Confira `src/main/resources/web/`: devem estar juntos `index.html`, `styles.css`, `app.js` e `cadastros.js`. Rode **Clean and Build**. |
| Erro ao carregar `schema.sql` | Execute a partir da pasta que contém `pom.xml` e `scripts/schema.sql`. |
| Seletores vazios | Cadastre um equipamento ativo e usuários. Para atribuir OS, vincule um usuário técnico em **Cadastros → Técnicos**. |
| `release version 24 not supported` | Confira o JDK usado pelo Maven: o compilador deve dar suporte a Java 24. |

## Roteiro de demonstração

1. Abra três ordens para o mesmo equipamento, por exemplo: **disjuntor desarma**, **sinaleiro pisca** e **inspeção preventiva**.
2. Atribua a primeira OS a Rafael usando Mariana como gestora. Atribua as outras duas ao mesmo técnico, na ordem desejada, e confira as posições 1 e 2 em **Fila dos técnicos**.
3. Na primeira OS, registre diagnóstico, inicie a execução, registre uma intervenção e finalize o reparo.
4. Confira o resultado esperado: primeira OS com **Reparo finalizado**, segunda **Atribuída** e terceira na primeira posição da fila.
5. Envie a primeira OS para aprovação e encerre como gestora. Reinicie o aplicativo e confira se os registros, os status e a fila persistiram.

## Estrutura

| Caminho | Responsabilidade |
| --- | --- |
| `src/main/java/br/com/manutencao/domain` | Entidades, regras de domínio e contratos dos repositórios |
| `src/main/java/br/com/manutencao/application` | Casos de uso, serviços e contratos de transação |
| `src/main/java/br/com/manutencao/infrastructure/persistence/sqlite` | Repositórios SQLite, conexão e inicialização |
| `src/main/java/br/com/manutencao/presentation` | Entradas da CLI e servidor HTTP local |
| `src/main/resources/web` | HTML, CSS e JavaScript |
| `scripts/schema.sql` | Estrutura inicial do banco |

A interface web é destinada à execução local em `127.0.0.1`. A seleção de usuários nos formulários não equivale a autenticação para acesso externo.
