package br.com.manutencao.presentation.cli;

import br.com.manutencao.application.service.EquipamentoService;
import br.com.manutencao.application.service.OrdemServicoService;
import br.com.manutencao.application.service.TecnicoService;
import br.com.manutencao.application.service.UsuarioService;
import br.com.manutencao.application.transaction.GerenciadorTransacao;

import br.com.manutencao.domain.repository.EquipamentoRepository;
import br.com.manutencao.domain.repository.FilaTecnicoRepository;
import br.com.manutencao.domain.repository.OrdemServicoRepository;
import br.com.manutencao.domain.repository.TecnicoRepository;
import br.com.manutencao.domain.repository.UsuarioRepository;

import br.com.manutencao.infrastructure.persistence.sqlite.DatabaseInitializer;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteEquipamentoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteFilaTecnicoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteGerenciadorTransacao;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteOrdemServicoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteTecnicoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteUsuarioRepository;

public class SistemaManutencao {

    public static void main(String[] args) {

        try {

            // 1. Inicializa a estrutura do banco.
            DatabaseInitializer.initialize();

            // 2. Cria os repositories concretos.
            EquipamentoRepository equipamentoRepository =
                    new SqliteEquipamentoRepository();

            UsuarioRepository usuarioRepository =
                    new SqliteUsuarioRepository();

            TecnicoRepository tecnicoRepository =
                    new SqliteTecnicoRepository();

            OrdemServicoRepository ordemServicoRepository =
                    new SqliteOrdemServicoRepository();

            FilaTecnicoRepository filaTecnicoRepository =
                    new SqliteFilaTecnicoRepository();

            // 3. Cria o gerenciador de transações.
            GerenciadorTransacao gerenciadorTransacao =
                    new SqliteGerenciadorTransacao();

            // 4. Conecta as dependências aos serviços.
            EquipamentoService equipamentoService =
                    new EquipamentoService(
                            equipamentoRepository,
                            ordemServicoRepository
                    );

            UsuarioService usuarioService =
                    new UsuarioService(
                            usuarioRepository
                    );

            TecnicoService tecnicoService =
                    new TecnicoService(
                            tecnicoRepository,
                            usuarioRepository
                    );

            OrdemServicoService ordemServicoService =
                    new OrdemServicoService(
                            ordemServicoRepository,
                            equipamentoRepository,
                            usuarioRepository,
                            tecnicoRepository,
                            filaTecnicoRepository,
                            gerenciadorTransacao
                    );

            // 5. Inicia a interface de terminal.
            MenuPrincipal menuPrincipal =
                    new MenuPrincipal(
                            equipamentoService,
                            usuarioService,
                            tecnicoService,
                            ordemServicoService
                    );

            menuPrincipal.executar();

        } catch (Exception e) {

            System.out.println(
                    "Erro ao iniciar sistema: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}