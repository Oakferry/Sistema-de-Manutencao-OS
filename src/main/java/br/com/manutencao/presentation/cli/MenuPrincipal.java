package br.com.manutencao.presentation.cli;

import br.com.manutencao.application.service.EquipamentoService;
import br.com.manutencao.application.service.OrdemServicoService;
import br.com.manutencao.application.service.TecnicoService;
import br.com.manutencao.application.service.UsuarioService;
import br.com.manutencao.domain.ordemservico.Criticidade;
import br.com.manutencao.domain.ordemservico.OrdemServico;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Tecnico;

import java.util.Scanner;

public class MenuPrincipal {

    private final EquipamentoService equipamentoService;
    private final UsuarioService usuarioService;
    private final TecnicoService tecnicoService;
    private final OrdemServicoService ordemServicoService;

    private final Scanner scanner;

    public MenuPrincipal(
            EquipamentoService equipamentoService,
            UsuarioService usuarioService,
            TecnicoService tecnicoService,
            OrdemServicoService ordemServicoService) {

        this.equipamentoService =
                equipamentoService;

        this.usuarioService =
                usuarioService;

        this.tecnicoService =
                tecnicoService;

        this.ordemServicoService =
                ordemServicoService;

        this.scanner =
                new Scanner(System.in);
    }

    public void executar() {

        int opcao;

        do {

            exibirMenuPrincipal();

            opcao = lerInteiro(
                    "Escolha uma opção: "
            );

            try {

                switch (opcao) {

                    case 1 ->
                        menuEquipamentos();

                    case 2 ->
                        menuUsuarios();

                    case 3 ->
                        menuTecnicos();

                    case 4 ->
                        menuOrdensServico();

                    case 0 ->
                        System.out.println(
                                "Sistema encerrado."
                        );

                    default ->
                        System.out.println(
                                "Opção inválida."
                        );
                }

            } catch (Exception e) {

                exibirErro(e);
            }

        } while (opcao != 0);
    }

    private void exibirMenuPrincipal() {

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                " SISTEMA DE MANUTENÇÃO"
        );
        System.out.println(
                "========================================"
        );

        System.out.println("1 - Equipamentos");
        System.out.println("2 - Usuários");
        System.out.println("3 - Técnicos");
        System.out.println("4 - Ordens de Serviço");
        System.out.println("0 - Sair");

        System.out.println(
                "========================================"
        );
    }

    // =========================================================
    // EQUIPAMENTOS
    // =========================================================

    private void menuEquipamentos() {

        int opcao;

        do {

            System.out.println();
            System.out.println(
                    "========== EQUIPAMENTOS =========="
            );

            System.out.println(
                    "1 - Cadastrar equipamento"
            );

            System.out.println(
                    "2 - Listar equipamentos"
            );

            System.out.println(
                    "3 - Consultar equipamento"
            );

            System.out.println(
                    "4 - Editar equipamento"
            );

            System.out.println(
                    "5 - Inativar equipamento"
            );

            System.out.println(
                    "6 - Ativar equipamento"
            );

            System.out.println(
                    "7 - Excluir equipamento"
            );

            System.out.println(
                    "0 - Voltar"
            );

            opcao = lerInteiro(
                    "Escolha uma opção: "
            );

            try {

                switch (opcao) {

                    case 1 ->
                        cadastrarEquipamento();

                    case 2 ->
                        listarEquipamentos();

                    case 3 ->
                        consultarEquipamento();

                    case 4 ->
                        editarEquipamento();

                    case 5 ->
                        inativarEquipamento();

                    case 6 ->
                        ativarEquipamento();

                    case 7 ->
                        excluirEquipamento();

                    case 0 -> {
                    }

                    default ->
                        System.out.println(
                                "Opção inválida."
                        );
                }

            } catch (Exception e) {

                exibirErro(e);
            }

        } while (opcao != 0);
    }

    private void cadastrarEquipamento() {

        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();

        System.out.print("Tipo: ");
        String tipo = scanner.nextLine();

        System.out.print("Identificador: ");
        String identificador =
                scanner.nextLine();

        var equipamento =
                equipamentoService.cadastrar(
                        nome,
                        descricao,
                        tipo,
                        identificador
                );

        System.out.println(
                "Equipamento cadastrado. ID: "
                        + equipamento.getId()
        );
    }

    private void listarEquipamentos() {

        var equipamentos =
                equipamentoService.listarTodos();

        if (equipamentos.isEmpty()) {

            System.out.println(
                    "Nenhum equipamento cadastrado."
            );

            return;
        }

        for (var equipamento : equipamentos) {

            System.out.println(
                    "----------------------------------------"
            );

            System.out.println(
                    "ID: " + equipamento.getId()
            );

            System.out.println(
                    "Nome: " + equipamento.getNome()
            );

            System.out.println(
                    "Descrição: "
                            + equipamento.getDescricao()
            );

            System.out.println(
                    "Tipo: "
                            + equipamento.getTipo()
            );

            System.out.println(
                    "Identificador: "
                            + equipamento.getIdentificador()
            );

            System.out.println(
                    "Status: "
                            + equipamento.getStatus()
            );
        }
    }

    private void consultarEquipamento() {

        Long id =
                lerLong(
                        "ID do equipamento: "
                );

        var equipamento =
                equipamentoService.buscarPorId(id);

        System.out.println(
                "ID: " + equipamento.getId()
        );

        System.out.println(
                "Nome: " + equipamento.getNome()
        );

        System.out.println(
                "Descrição: "
                        + equipamento.getDescricao()
        );

        System.out.println(
                "Tipo: "
                        + equipamento.getTipo()
        );

        System.out.println(
                "Identificador: "
                        + equipamento.getIdentificador()
        );

        System.out.println(
                "Status: "
                        + equipamento.getStatus()
        );
    }

    private void editarEquipamento() {

        Long id =
                lerLong(
                        "ID do equipamento: "
                );

        var atual =
                equipamentoService.buscarPorId(id);

        System.out.println(
                "Nome atual: "
                        + atual.getNome()
        );

        System.out.print("Novo nome: ");
        String nome = scanner.nextLine();

        System.out.print("Nova descrição: ");
        String descricao = scanner.nextLine();

        System.out.print("Novo tipo: ");
        String tipo = scanner.nextLine();

        System.out.print(
                "Novo identificador: "
        );

        String identificador =
                scanner.nextLine();

        equipamentoService.editar(
                id,
                nome,
                descricao,
                tipo,
                identificador
        );

        System.out.println(
                "Equipamento atualizado."
        );
    }

    private void inativarEquipamento() {

        Long id =
                lerLong(
                        "ID do equipamento: "
                );

        equipamentoService.inativar(id);

        System.out.println(
                "Equipamento inativado."
        );
    }

    private void ativarEquipamento() {

        Long id =
                lerLong(
                        "ID do equipamento: "
                );

        equipamentoService.ativar(id);

        System.out.println(
                "Equipamento ativado."
        );
    }

    private void excluirEquipamento() {

        Long id =
                lerLong(
                        "ID do equipamento: "
                );

        equipamentoService.excluir(id);

        System.out.println(
                "Equipamento excluído."
        );
    }

    // =========================================================
    // USUÁRIOS
    // =========================================================

    private void menuUsuarios() {

        int opcao;

        do {

            System.out.println();
            System.out.println(
                    "========== USUÁRIOS =========="
            );

            System.out.println(
                    "1 - Cadastrar usuário"
            );

            System.out.println(
                    "2 - Listar usuários"
            );

            System.out.println(
                    "3 - Consultar usuário"
            );

            System.out.println(
                    "4 - Editar usuário"
            );

            System.out.println(
                    "5 - Alterar perfil"
            );

            System.out.println(
                    "0 - Voltar"
            );

            opcao = lerInteiro(
                    "Escolha uma opção: "
            );

            try {

                switch (opcao) {

                    case 1 ->
                        cadastrarUsuario();

                    case 2 ->
                        listarUsuarios();

                    case 3 ->
                        consultarUsuario();

                    case 4 ->
                        editarUsuario();

                    case 5 ->
                        alterarPerfilUsuario();

                    case 0 -> {
                    }

                    default ->
                        System.out.println(
                                "Opção inválida."
                        );
                }

            } catch (Exception e) {

                exibirErro(e);
            }

        } while (opcao != 0);
    }

    private void cadastrarUsuario() {

        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Login: ");
        String login = scanner.nextLine();

        PerfilUsuario perfil =
                lerPerfilUsuario();

        var usuario =
                usuarioService.criarUsuario(
                        nome,
                        login,
                        perfil
                );

        System.out.println(
                "Usuário cadastrado. ID: "
                        + usuario.getId()
        );

        if (perfil == PerfilUsuario.TECNICO) {

            System.out.println(
                    "Use o menu Técnicos para criar "
                    + "o cadastro técnico."
            );
        }
    }

    private void listarUsuarios() {

        var usuarios =
                usuarioService.listarTodos();

        if (usuarios.isEmpty()) {

            System.out.println(
                    "Nenhum usuário cadastrado."
            );

            return;
        }

        for (var usuario : usuarios) {

            System.out.println(
                    "----------------------------------------"
            );

            System.out.println(
                    "ID: " + usuario.getId()
            );

            System.out.println(
                    "Nome: " + usuario.getNome()
            );

            System.out.println(
                    "Login: " + usuario.getLogin()
            );

            System.out.println(
                    "Perfil: "
                            + usuario.getPerfil()
            );
        }
    }

    private void consultarUsuario() {

        Long id =
                lerLong(
                        "ID do usuário: "
                );

        var usuario =
                usuarioService.buscarPorId(id);

        System.out.println(
                "ID: " + usuario.getId()
        );

        System.out.println(
                "Nome: " + usuario.getNome()
        );

        System.out.println(
                "Login: " + usuario.getLogin()
        );

        System.out.println(
                "Perfil: "
                        + usuario.getPerfil()
        );
    }

    private void editarUsuario() {

        Long id =
                lerLong(
                        "ID do usuário: "
                );

        var atual =
                usuarioService.buscarPorId(id);

        System.out.println(
                "Nome atual: "
                        + atual.getNome()
        );

        System.out.println(
                "Login atual: "
                        + atual.getLogin()
        );

        System.out.print("Novo nome: ");
        String nome = scanner.nextLine();

        System.out.print("Novo login: ");
        String login = scanner.nextLine();

        usuarioService.editarUsuario(
                id,
                nome,
                login
        );

        System.out.println(
                "Usuário atualizado."
        );
    }

    private void alterarPerfilUsuario() {

        Long id =
                lerLong(
                        "ID do usuário: "
                );

        PerfilUsuario perfil =
                lerPerfilUsuario();

        usuarioService.definirPerfil(
                id,
                perfil
        );

        System.out.println(
                "Perfil atualizado."
        );
    }

    private PerfilUsuario lerPerfilUsuario() {

        while (true) {

            System.out.println(
                    "1 - GESTOR"
            );

            System.out.println(
                    "2 - TÉCNICO"
            );

            int opcao =
                    lerInteiro(
                            "Perfil: "
                    );

            if (opcao == 1) {
                return PerfilUsuario.GESTOR;
            }

            if (opcao == 2) {
                return PerfilUsuario.TECNICO;
            }

            System.out.println(
                    "Perfil inválido."
            );
        }
    }

    // =========================================================
    // TÉCNICOS
    // =========================================================

    private void menuTecnicos() {

        int opcao;

        do {

            System.out.println();
            System.out.println(
                    "========== TÉCNICOS =========="
            );

            System.out.println(
                    "1 - Cadastrar técnico"
            );

            System.out.println(
                    "2 - Listar técnicos"
            );

            System.out.println(
                    "3 - Consultar técnico"
            );

            System.out.println(
                    "4 - Listar disponíveis"
            );

            System.out.println(
                    "5 - Marcar disponível"
            );

            System.out.println(
                    "6 - Marcar em atendimento"
            );

            System.out.println(
                    "7 - Marcar ausente"
            );

            System.out.println(
                    "0 - Voltar"
            );

            opcao =
                    lerInteiro(
                            "Escolha uma opção: "
                    );

            try {

                switch (opcao) {

                    case 1 ->
                        cadastrarTecnico();

                    case 2 ->
                        listarTecnicos();

                    case 3 ->
                        consultarTecnico();

                    case 4 ->
                        listarTecnicosDisponiveis();

                    case 5 ->
                        marcarTecnicoDisponivel();

                    case 6 ->
                        marcarTecnicoEmAtendimento();

                    case 7 ->
                        marcarTecnicoAusente();

                    case 0 -> {
                    }

                    default ->
                        System.out.println(
                                "Opção inválida."
                        );
                }

            } catch (Exception e) {

                exibirErro(e);
            }

        } while (opcao != 0);
    }

    private void cadastrarTecnico() {

        Long usuarioId =
                lerLong(
                        "ID do usuário TECNICO: "
                );

        var tecnico =
                tecnicoService.criarTecnico(
                        usuarioId
                );

        System.out.println(
                "Técnico cadastrado. ID: "
                        + tecnico.getId()
        );
    }

    private void listarTecnicos() {

        var tecnicos =
                tecnicoService.listarTodos();

        if (tecnicos.isEmpty()) {

            System.out.println(
                    "Nenhum técnico cadastrado."
            );

            return;
        }

        for (var tecnico : tecnicos) {

            exibirTecnico(tecnico);
        }
    }

    private void consultarTecnico() {

        Long id =
                lerLong(
                        "ID do técnico: "
                );

        exibirTecnico(
                tecnicoService.buscarPorId(id)
        );
    }

    private void listarTecnicosDisponiveis() {

        var tecnicos =
                tecnicoService.listarDisponiveis();

        if (tecnicos.isEmpty()) {

            System.out.println(
                    "Nenhum técnico disponível."
            );

            return;
        }

        for (var tecnico : tecnicos) {

            exibirTecnico(tecnico);
        }
    }

    private void marcarTecnicoDisponivel() {

        Long id =
                lerLong(
                        "ID do técnico: "
                );

        tecnicoService.ficarDisponivel(id);

        System.out.println(
                "Técnico disponível."
        );
    }

    private void marcarTecnicoEmAtendimento() {

        Long id =
                lerLong(
                        "ID do técnico: "
                );

        tecnicoService.entrarEmAtendimento(id);

        System.out.println(
                "Técnico em atendimento."
        );
    }

    private void marcarTecnicoAusente() {

        Long id =
                lerLong(
                        "ID do técnico: "
                );

        tecnicoService.ficarAusente(id);

        System.out.println(
                "Técnico ausente."
        );
    }

    private void exibirTecnico(
            Tecnico tecnico) {

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "ID Técnico: "
                        + tecnico.getId()
        );

        System.out.println(
                "Usuário: "
                        + tecnico
                                .getUsuario()
                                .getNome()
        );

        System.out.println(
                "Login: "
                        + tecnico
                                .getUsuario()
                                .getLogin()
        );

        System.out.println(
                "Disponibilidade: "
                        + tecnico.getDisponibilidade()
        );
    }

    // =========================================================
    // ORDENS DE SERVIÇO
    // =========================================================

    private void menuOrdensServico() {

        int opcao;

        do {

            System.out.println();
            System.out.println(
                    "========== ORDENS DE SERVIÇO =========="
            );

            System.out.println(
                    "1 - Abrir OS"
            );

            System.out.println(
                    "2 - Listar OS"
            );

            System.out.println(
                    "3 - Consultar OS"
            );

            System.out.println(
                    "4 - Atribuir técnico"
            );

            System.out.println(
                    "5 - Reatribuir técnico"
            );

            System.out.println(
                    "6 - Registrar diagnóstico"
            );

            System.out.println(
                    "7 - Iniciar execução"
            );

            System.out.println(
                    "8 - Registrar intervenção"
            );

            System.out.println(
                    "9 - Aguardar peça"
            );

            System.out.println(
                    "10 - Retomar execução"
            );

            System.out.println(
                    "11 - Finalizar reparo"
            );

            System.out.println(
                    "12 - Submeter para aprovação"
            );

            System.out.println(
                    "13 - Encerrar OS"
            );

            System.out.println(
                    "14 - Cancelar OS"
            );

            System.out.println(
                    "15 - Registrar material/peça"
            );

            System.out.println(
                    "0 - Voltar"
            );

            opcao =
                    lerInteiro(
                            "Escolha uma opção: "
                    );

            try {

                switch (opcao) {

                    case 1 ->
                        abrirOrdemServico();

                    case 2 ->
                        listarOrdensServico();

                    case 3 ->
                        consultarOrdemServico();

                    case 4 ->
                        atribuirTecnicoOrdemServico();

                    case 5 ->
                        reatribuirTecnicoOrdemServico();

                    case 6 ->
                        registrarDiagnostico();

                    case 7 ->
                        iniciarExecucao();

                    case 8 ->
                        registrarIntervencao();

                    case 9 ->
                        aguardarPeca();

                    case 10 ->
                        retomarExecucao();

                    case 11 ->
                        finalizarReparo();

                    case 12 ->
                        submeterParaAprovacao();

                    case 13 ->
                        encerrarOrdemServico();

                    case 14 ->
                        cancelarOrdemServico();

                    case 15 ->
                        registrarMaterialIntervencao();

                    case 0 -> {
                    }

                    default ->
                        System.out.println(
                                "Opção inválida."
                        );
                }

            } catch (Exception e) {

                exibirErro(e);
            }

        } while (opcao != 0);
    }

    private void abrirOrdemServico() {

        Long equipamentoId =
                lerLong(
                        "ID do equipamento: "
                );

        Long usuarioId =
                lerLong(
                        "ID do usuário que abre a OS: "
                );

        System.out.print(
                "Descrição do problema: "
        );

        String descricao =
                scanner.nextLine();

        Criticidade criticidade =
                lerCriticidade();

        var ordem =
                ordemServicoService
                        .abrirOrdemServico(
                                equipamentoId,
                                usuarioId,
                                descricao,
                                criticidade
                        );

        System.out.println(
                "OS aberta. ID: "
                        + ordem.getId()
        );
    }

    private void listarOrdensServico() {

        var ordens =
                ordemServicoService.listarTodos();

        if (ordens.isEmpty()) {

            System.out.println(
                    "Nenhuma OS cadastrada."
            );

            return;
        }

        for (var ordem : ordens) {

            exibirResumoOrdemServico(
                    ordem
            );
        }
    }

    private void consultarOrdemServico() {

        Long id =
                lerLong(
                        "ID da OS: "
                );

        OrdemServico ordem =
                ordemServicoService
                        .buscarPorId(id);

        exibirDetalhesOrdemServico(
                ordem
        );
    }

    private void atribuirTecnicoOrdemServico() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long gestorId =
                lerLong(
                        "ID do gestor: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        ordemServicoService.atribuirTecnico(
                ordemId,
                gestorId,
                tecnicoId
        );

        System.out.println(
                "Técnico atribuído."
        );
    }

    private void reatribuirTecnicoOrdemServico() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long gestorId =
                lerLong(
                        "ID do gestor: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do novo técnico: "
                );

        ordemServicoService.reatribuirTecnico(
                ordemId,
                gestorId,
                tecnicoId
        );

        System.out.println(
                "Técnico reatribuído."
        );
    }

    private void registrarDiagnostico() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        System.out.print(
                "Parecer técnico: "
        );

        String parecer =
                scanner.nextLine();

        System.out.print(
                "Causa raiz: "
        );

        String causaRaiz =
                scanner.nextLine();

        ordemServicoService.registrarDiagnostico(
                ordemId,
                tecnicoId,
                parecer,
                causaRaiz
        );

        System.out.println(
                "Diagnóstico registrado."
        );
    }

    private void iniciarExecucao() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        ordemServicoService.iniciarExecucao(
                ordemId,
                tecnicoId
        );

        System.out.println(
                "Execução iniciada."
        );
    }

    private void registrarIntervencao() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        System.out.print(
                "Descrição da intervenção: "
        );

        String descricao =
                scanner.nextLine();

        double horas =
                lerDouble(
                        "Horas trabalhadas: "
                );

        ordemServicoService
                .registrarIntervencao(
                        ordemId,
                        tecnicoId,
                        descricao,
                        horas
                );

        System.out.println(
                "Intervenção registrada."
        );

        System.out.println(
                "Consulte a OS para visualizar "
                + "o ID da intervenção."
        );
    }

    private void registrarMaterialIntervencao() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        Long intervencaoId =
                lerLong(
                        "ID da intervenção: "
                );

        System.out.print(
                "Material/peça: "
        );

        String descricao =
                scanner.nextLine();

        double quantidade =
                lerDouble(
                        "Quantidade: "
                );

        System.out.print(
                "Unidade: "
        );

        String unidade =
                scanner.nextLine();

        ordemServicoService
                .adicionarMaterialIntervencao(
                        ordemId,
                        tecnicoId,
                        intervencaoId,
                        descricao,
                        quantidade,
                        unidade
                );

        System.out.println(
                "Material/peça registrado."
        );
    }

    private void aguardarPeca() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        ordemServicoService.aguardarPeca(
                ordemId,
                tecnicoId
        );

        System.out.println(
                "OS aguardando peça."
        );
    }

    private void retomarExecucao() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        ordemServicoService.retomarExecucao(
                ordemId,
                tecnicoId
        );

        System.out.println(
                "Execução retomada."
        );
    }

    private void finalizarReparo() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        ordemServicoService.finalizarReparo(
                ordemId,
                tecnicoId
        );

        System.out.println(
                "Reparo finalizado."
        );
    }

    private void submeterParaAprovacao() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long tecnicoId =
                lerLong(
                        "ID do técnico: "
                );

        ordemServicoService
                .submeterParaAprovacao(
                        ordemId,
                        tecnicoId
                );

        System.out.println(
                "OS enviada para aprovação."
        );
    }

    private void encerrarOrdemServico() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long gestorId =
                lerLong(
                        "ID do gestor: "
                );

        ordemServicoService.encerrar(
                ordemId,
                gestorId
        );

        System.out.println(
                "OS encerrada."
        );
    }

    private void cancelarOrdemServico() {

        Long ordemId =
                lerLong(
                        "ID da OS: "
                );

        Long gestorId =
                lerLong(
                        "ID do gestor: "
                );

        ordemServicoService.cancelar(
                ordemId,
                gestorId
        );

        System.out.println(
                "OS cancelada."
        );
    }

    private void exibirResumoOrdemServico(
            OrdemServico ordem) {

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "OS: " + ordem.getId()
        );

        System.out.println(
                "Equipamento: "
                        + ordem
                                .getEquipamento()
                                .getNome()
        );

        System.out.println(
                "Criticidade: "
                        + ordem.getCriticidade()
        );

        System.out.println(
                "Status: "
                        + ordem.getStatus()
        );

        if (ordem.getTecnicoResponsavel()
                != null) {

            System.out.println(
                    "Técnico: "
                            + ordem
                                    .getTecnicoResponsavel()
                                    .getUsuario()
                                    .getNome()
            );

        } else {

            System.out.println(
                    "Técnico: não atribuído"
            );
        }
    }

    private void exibirDetalhesOrdemServico(
            OrdemServico ordem) {

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                " OS #" + ordem.getId()
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "Equipamento: "
                        + ordem
                                .getEquipamento()
                                .getNome()
        );

        System.out.println(
                "Aberta por: "
                        + ordem
                                .getUsuarioAbertura()
                                .getNome()
        );

        System.out.println(
                "Problema: "
                        + ordem.getDescricaoProblema()
        );

        System.out.println(
                "Criticidade: "
                        + ordem.getCriticidade()
        );

        System.out.println(
                "Status: "
                        + ordem.getStatus()
        );

        System.out.println(
                "Data abertura: "
                        + ordem.getDataAbertura()
        );

        if (ordem.getTecnicoResponsavel()
                != null) {

            System.out.println(
                    "Técnico responsável: "
                            + ordem
                                    .getTecnicoResponsavel()
                                    .getUsuario()
                                    .getNome()
            );
        }

        if (ordem.getDiagnostico()
                != null) {

            System.out.println();
            System.out.println(
                    "--- DIAGNÓSTICO ---"
            );

            System.out.println(
                    "Parecer: "
                            + ordem
                                    .getDiagnostico()
                                    .getParecer()
            );

            System.out.println(
                    "Causa raiz: "
                            + ordem
                                    .getDiagnostico()
                                    .getCausaRaiz()
            );
        }

        System.out.println();
        System.out.println(
                "--- INTERVENÇÕES ---"
        );

        if (ordem.getIntervencoes()
                .isEmpty()) {

            System.out.println(
                    "Nenhuma intervenção."
            );

            return;
        }

        for (var intervencao
                : ordem.getIntervencoes()) {

            System.out.println(
                    "----------------------------------------"
            );

            System.out.println(
                    "ID intervenção: "
                            + intervencao.getId()
            );

            System.out.println(
                    "Descrição: "
                            + intervencao.getDescricao()
            );

            System.out.println(
                    "Horas: "
                            + intervencao
                                    .getHorasTrabalhadas()
            );

            System.out.println(
                    "Data: "
                            + intervencao.getDataHora()
            );

            System.out.println(
                    "Técnico: "
                            + intervencao
                                    .getTecnico()
                                    .getUsuario()
                                    .getNome()
            );

            if (!intervencao
                    .getMateriais()
                    .isEmpty()) {

                System.out.println(
                        "Materiais:"
                );

                for (var material
                        : intervencao
                                .getMateriais()) {

                    System.out.println(
                            " - "
                                    + material.getDescricao()
                                    + ": "
                                    + material.getQuantidade()
                                    + " "
                                    + material.getUnidade()
                    );
                }
            }
        }
    }

    // =========================================================
    // LEITURA
    // =========================================================

    private Criticidade lerCriticidade() {

        while (true) {

            System.out.println(
                    "1 - BAIXA"
            );

            System.out.println(
                    "2 - MÉDIA"
            );

            System.out.println(
                    "3 - ALTA"
            );

            System.out.println(
                    "4 - CRÍTICA"
            );

            int opcao =
                    lerInteiro(
                            "Criticidade: "
                    );

            switch (opcao) {

                case 1:
                    return Criticidade.BAIXA;

                case 2:
                    return Criticidade.MEDIA;

                case 3:
                    return Criticidade.ALTA;

                case 4:
                    return Criticidade.CRITICA;

                default:
                    System.out.println(
                            "Criticidade inválida."
                    );
            }
        }
    }

    private int lerInteiro(
            String mensagem) {

        while (true) {

            System.out.print(
                    mensagem
            );

            try {

                return Integer.parseInt(
                        scanner.nextLine()
                );

            } catch (NumberFormatException e) {

                System.out.println(
                        "Digite um número válido."
                );
            }
        }
    }

    private Long lerLong(
            String mensagem) {

        while (true) {

            System.out.print(
                    mensagem
            );

            try {

                return Long.parseLong(
                        scanner.nextLine()
                );

            } catch (NumberFormatException e) {

                System.out.println(
                        "Digite um número válido."
                );
            }
        }
    }

    private double lerDouble(
            String mensagem) {

        while (true) {

            System.out.print(
                    mensagem
            );

            String entrada =
                    scanner
                            .nextLine()
                            .replace(",", ".");

            try {

                return Double.parseDouble(
                        entrada
                );

            } catch (NumberFormatException e) {

                System.out.println(
                        "Digite um número válido."
                );
            }
        }
    }

    private void exibirErro(
            Exception e) {

        System.out.println();
        System.out.println(
                "Erro: "
                        + e.getMessage()
        );
        System.out.println();
    }
}