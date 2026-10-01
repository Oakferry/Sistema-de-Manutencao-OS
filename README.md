# Sistema de Gestão de Ordens de Serviço e Manutenção de Equipamentos

Projeto acadêmico desenvolvido para a disciplina de Arquitetura de Software.

O sistema tem como objetivo gerenciar equipamentos, usuários, técnicos e ordens de serviço relacionadas à manutenção de equipamentos.

---

## Funcionalidades

### Equipamentos

- Cadastro de equipamentos
- Consulta de equipamentos
- Listagem de equipamentos
- Edição
- Ativação
- Inativação
- Exclusão quando não houver histórico de ordens de serviço

### Usuários

- Cadastro de usuários
- Consulta
- Listagem
- Edição
- Definição de perfil

Perfis disponíveis:

- GESTOR
- TECNICO

### Técnicos

- Cadastro técnico associado a um usuário
- Consulta
- Listagem
- Controle de disponibilidade

Estados de disponibilidade:

- DISPONIVEL
- EM_ATENDIMENTO
- AUSENTE

### Ordens de Serviço

- Abertura de ordem de serviço
- Definição de criticidade
- Atribuição de técnico
- Reatribuição de técnico
- Registro de diagnóstico
- Registro de causa raiz
- Início da execução
- Registro de intervenções
- Registro de horas trabalhadas
- Registro de materiais e peças
- Mudança para aguardando peça
- Retomada da execução
- Finalização do reparo
- Submissão para aprovação
- Encerramento pelo gestor
- Cancelamento
- Consulta e listagem

---

## Fluxo da Ordem de Serviço

O ciclo de vida principal de uma ordem de serviço é:

```text
ABERTA
  ↓
ATRIBUIDA
  ↓
EM_EXECUCAO
  ↕
AGUARDANDO_PECA
  ↓
REPARO_FINALIZADO
  ↓
AGUARDANDO_APROVACAO
  ↓
ENCERRADA
