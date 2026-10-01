package br.com.manutencao.presentation.cli;

import br.com.manutencao.application.service.EquipamentoService;
import br.com.manutencao.application.service.OrdemServicoService;
import br.com.manutencao.application.service.TecnicoService;
import br.com.manutencao.application.service.UsuarioService;

import br.com.manutencao.domain.repository.EquipamentoRepository;
import br.com.manutencao.domain.repository.OrdemServicoRepository;
import br.com.manutencao.domain.repository.TecnicoRepository;
import br.com.manutencao.domain.repository.UsuarioRepository;

import br.com.manutencao.infrastructure.persistence.sqlite.DatabaseInitializer;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteEquipamentoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteOrdemServicoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteTecnicoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteUsuarioRepository;

public class SistemaManutencao {

    public static void main(String[] args) {

        try {

            // 1. Prepara o banco de dados
            DatabaseInitializer.initialize();

            // 2. Repositories concretos
            EquipamentoRepository equipamentoRepository =
                    new SqliteEquipamentoRepository();

            UsuarioRepository usuarioRepository =
                    new SqliteUsuarioRepository();

            TecnicoRepository tecnicoRepository =
                    new SqliteTecnicoRepository();

            OrdemServicoRepository ordemServicoRepository =
                    new SqliteOrdemServicoRepository();

            // 3. Services da aplicação
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
                            tecnicoRepository
                    );

            // 4. Inicia a interface CLI
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