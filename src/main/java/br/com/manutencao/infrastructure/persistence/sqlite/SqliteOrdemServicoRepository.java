package br.com.manutencao.infrastructure.persistence.sqlite;

import br.com.manutencao.domain.equipamento.Equipamento;
import br.com.manutencao.domain.equipamento.StatusEquipamento;
import br.com.manutencao.domain.ordemservico.Criticidade;
import br.com.manutencao.domain.ordemservico.Diagnostico;
import br.com.manutencao.domain.ordemservico.Intervencao;
import br.com.manutencao.domain.ordemservico.MaterialUtilizado;
import br.com.manutencao.domain.ordemservico.OrdemServico;
import br.com.manutencao.domain.ordemservico.StatusOrdemServico;
import br.com.manutencao.domain.repository.OrdemServicoRepository;
import br.com.manutencao.domain.usuario.DisponibilidadeTecnico;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Tecnico;
import br.com.manutencao.domain.usuario.Usuario;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteOrdemServicoRepository
        implements OrdemServicoRepository {

    @Override
    public OrdemServico salvar(
            OrdemServico ordemServico) {

        try (Connection connection =
                     ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {

                OrdemServico persistida;

                if (ordemServico.getId() == null) {
                    persistida = inserir(
                            connection,
                            ordemServico
                    );
                } else {
                    persistida = atualizar(
                            connection,
                            ordemServico
                    );
                }

                salvarDiagnostico(
                        connection,
                        persistida
                );

                salvarIntervencoes(
                        connection,
                        persistida
                );

                connection.commit();

                return persistida;

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao salvar ordem de serviço.",
                    e
            );
        }
    }

    private OrdemServico inserir(
            Connection connection,
            OrdemServico ordemServico)
            throws SQLException {

        String sql = """
                INSERT INTO ordem_servico (
                    equipamento_id,
                    usuario_abertura_id,
                    tecnico_responsavel_id,
                    descricao_problema,
                    criticidade,
                    status,
                    data_abertura,
                    data_encerramento
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            preencherStatementOrdem(
                    statement,
                    ordemServico
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Não foi possível obter o ID da ordem de serviço."
                    );
                }

                Long id = keys.getLong(1);

                return new OrdemServico(
                        id,
                        ordemServico.getEquipamento(),
                        ordemServico.getUsuarioAbertura(),
                        ordemServico.getDescricaoProblema(),
                        ordemServico.getCriticidade(),
                        ordemServico.getStatus(),
                        ordemServico.getDataAbertura(),
                        ordemServico.getDataEncerramento(),
                        ordemServico.getTecnicoResponsavel(),
                        ordemServico.getDiagnostico(),
                        ordemServico.getIntervencoes()
                );
            }
        }
    }

    private OrdemServico atualizar(
            Connection connection,
            OrdemServico ordemServico)
            throws SQLException {

        String sql = """
                UPDATE ordem_servico
                SET equipamento_id = ?,
                    usuario_abertura_id = ?,
                    tecnico_responsavel_id = ?,
                    descricao_problema = ?,
                    criticidade = ?,
                    status = ?,
                    data_abertura = ?,
                    data_encerramento = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            preencherStatementOrdem(
                    statement,
                    ordemServico
            );

            statement.setLong(
                    9,
                    ordemServico.getId()
            );

            statement.executeUpdate();

            return ordemServico;
        }
    }

    private void preencherStatementOrdem(
            PreparedStatement statement,
            OrdemServico ordemServico)
            throws SQLException {

        statement.setLong(
                1,
                ordemServico
                        .getEquipamento()
                        .getId()
        );

        statement.setLong(
                2,
                ordemServico
                        .getUsuarioAbertura()
                        .getId()
        );

        if (ordemServico.getTecnicoResponsavel() == null) {

            statement.setNull(
                    3,
                    Types.INTEGER
            );

        } else {

            statement.setLong(
                    3,
                    ordemServico
                            .getTecnicoResponsavel()
                            .getId()
            );
        }

        statement.setString(
                4,
                ordemServico.getDescricaoProblema()
        );

        statement.setString(
                5,
                ordemServico.getCriticidade().name()
        );

        statement.setString(
                6,
                ordemServico.getStatus().name()
        );

        statement.setString(
                7,
                ordemServico
                        .getDataAbertura()
                        .toString()
        );

        if (ordemServico.getDataEncerramento() == null) {

            statement.setNull(
                    8,
                    Types.VARCHAR
            );

        } else {

            statement.setString(
                    8,
                    ordemServico
                            .getDataEncerramento()
                            .toString()
            );
        }
    }

    private void salvarDiagnostico(
            Connection connection,
            OrdemServico ordemServico)
            throws SQLException {

        String sqlExcluir = """
                DELETE FROM diagnostico
                WHERE ordem_servico_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sqlExcluir
                     )) {

            statement.setLong(
                    1,
                    ordemServico.getId()
            );

            statement.executeUpdate();
        }

        Diagnostico diagnostico =
                ordemServico.getDiagnostico();

        if (diagnostico == null) {
            return;
        }

        String sqlInserir = """
                INSERT INTO diagnostico (
                    ordem_servico_id,
                    tecnico_id,
                    parecer,
                    causa_raiz,
                    data_hora
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sqlInserir
                     )) {

            statement.setLong(
                    1,
                    ordemServico.getId()
            );

            statement.setLong(
                    2,
                    diagnostico
                            .getTecnico()
                            .getId()
            );

            statement.setString(
                    3,
                    diagnostico.getParecer()
            );

            statement.setString(
                    4,
                    diagnostico.getCausaRaiz()
            );

            statement.setString(
                    5,
                    diagnostico
                            .getDataHora()
                            .toString()
            );

            statement.executeUpdate();
        }
    }

    private void salvarIntervencoes(
            Connection connection,
            OrdemServico ordemServico)
            throws SQLException {

        String excluirMateriais = """
                DELETE FROM material_utilizado
                WHERE intervencao_id IN (
                    SELECT id
                    FROM intervencao
                    WHERE ordem_servico_id = ?
                )
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             excluirMateriais
                     )) {

            statement.setLong(
                    1,
                    ordemServico.getId()
            );

            statement.executeUpdate();
        }

        String excluirIntervencoes = """
                DELETE FROM intervencao
                WHERE ordem_servico_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             excluirIntervencoes
                     )) {

            statement.setLong(
                    1,
                    ordemServico.getId()
            );

            statement.executeUpdate();
        }

        for (Intervencao intervencao
                : ordemServico.getIntervencoes()) {

            Long intervencaoId =
                    inserirIntervencao(
                            connection,
                            ordemServico.getId(),
                            intervencao
                    );

            salvarMateriais(
                    connection,
                    intervencaoId,
                    intervencao.getMateriais()
            );
        }
    }

    private Long inserirIntervencao(
            Connection connection,
            Long ordemServicoId,
            Intervencao intervencao)
            throws SQLException {

        String sql = """
                INSERT INTO intervencao (
                    ordem_servico_id,
                    tecnico_id,
                    descricao,
                    horas_trabalhadas,
                    data_hora
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    ordemServicoId
            );

            statement.setLong(
                    2,
                    intervencao
                            .getTecnico()
                            .getId()
            );

            statement.setString(
                    3,
                    intervencao.getDescricao()
            );

            statement.setDouble(
                    4,
                    intervencao.getHorasTrabalhadas()
            );

            statement.setString(
                    5,
                    intervencao
                            .getDataHora()
                            .toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Não foi possível obter o ID da intervenção."
                    );
                }

                return keys.getLong(1);
            }
        }
    }

    private void salvarMateriais(
            Connection connection,
            Long intervencaoId,
            List<MaterialUtilizado> materiais)
            throws SQLException {

        String sql = """
                INSERT INTO material_utilizado (
                    intervencao_id,
                    descricao,
                    quantidade,
                    unidade
                )
                VALUES (?, ?, ?, ?)
                """;

        for (MaterialUtilizado material : materiais) {

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setLong(
                        1,
                        intervencaoId
                );

                statement.setString(
                        2,
                        material.getDescricao()
                );

                statement.setDouble(
                        3,
                        material.getQuantidade()
                );

                statement.setString(
                        4,
                        material.getUnidade()
                );

                statement.executeUpdate();
            }
        }
    }

    @Override
    public Optional<OrdemServico> buscarPorId(
            Long id) {

        String sql = """
                SELECT *
                FROM ordem_servico
                WHERE id = ?
                """;

        try (Connection connection =
                     ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (rs.next()) {

                    return Optional.of(
                            mapearOrdemServico(
                                    connection,
                                    rs
                            )
                    );
                }
            }

            return Optional.empty();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar ordem de serviço por ID.",
                    e
            );
        }
    }

    @Override
    public List<OrdemServico> listarTodos() {

        String sql = """
                SELECT *
                FROM ordem_servico
                ORDER BY data_abertura DESC
                """;

        return executarListagem(
                sql,
                null
        );
    }

    @Override
    public List<OrdemServico> listarPorStatus(
            StatusOrdemServico status) {

        String sql = """
                SELECT *
                FROM ordem_servico
                WHERE status = ?
                ORDER BY data_abertura DESC
                """;

        return executarListagem(
                sql,
                status.name()
        );
    }

    @Override
    public List<OrdemServico> listarPorEquipamento(
            Long equipamentoId) {

        String sql = """
                SELECT *
                FROM ordem_servico
                WHERE equipamento_id = ?
                ORDER BY data_abertura DESC
                """;

        return executarListagem(
                sql,
                equipamentoId
        );
    }

    @Override
    public List<OrdemServico> listarPorTecnico(
            Long tecnicoId) {

        String sql = """
                SELECT *
                FROM ordem_servico
                WHERE tecnico_responsavel_id = ?
                ORDER BY data_abertura DESC
                """;

        return executarListagem(
                sql,
                tecnicoId
        );
    }

    private List<OrdemServico> executarListagem(
            String sql,
            Object parametro) {

        List<OrdemServico> ordens =
                new ArrayList<>();

        try (Connection connection =
                     ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            if (parametro instanceof String valor) {
                statement.setString(
                        1,
                        valor
                );
            } else if (parametro instanceof Long valor) {
                statement.setLong(
                        1,
                        valor
                );
            }

            try (ResultSet rs =
                         statement.executeQuery()) {

                while (rs.next()) {

                    ordens.add(
                            mapearOrdemServico(
                                    connection,
                                    rs
                            )
                    );
                }
            }

            return ordens;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar ordens de serviço.",
                    e
            );
        }
    }

    private OrdemServico mapearOrdemServico(
            Connection connection,
            ResultSet rs)
            throws SQLException {

        Long ordemServicoId =
                rs.getLong("id");

        Equipamento equipamento =
                buscarEquipamento(
                        connection,
                        rs.getLong(
                                "equipamento_id"
                        )
                );

        Usuario usuarioAbertura =
                buscarUsuario(
                        connection,
                        rs.getLong(
                                "usuario_abertura_id"
                        )
                );

        Tecnico tecnicoResponsavel = null;

        long tecnicoId =
                rs.getLong(
                        "tecnico_responsavel_id"
                );

        if (!rs.wasNull()) {

            tecnicoResponsavel =
                    buscarTecnico(
                            connection,
                            tecnicoId
                    );
        }

        Diagnostico diagnostico =
                buscarDiagnostico(
                        connection,
                        ordemServicoId
                );

        List<Intervencao> intervencoes =
                buscarIntervencoes(
                        connection,
                        ordemServicoId
                );

        String textoDataEncerramento =
                rs.getString(
                        "data_encerramento"
                );

        LocalDateTime dataEncerramento = null;

        if (textoDataEncerramento != null) {

            dataEncerramento =
                    LocalDateTime.parse(
                            textoDataEncerramento
                    );
        }

        return new OrdemServico(
                ordemServicoId,
                equipamento,
                usuarioAbertura,
                rs.getString(
                        "descricao_problema"
                ),
                Criticidade.valueOf(
                        rs.getString(
                                "criticidade"
                        )
                ),
                StatusOrdemServico.valueOf(
                        rs.getString(
                                "status"
                        )
                ),
                LocalDateTime.parse(
                        rs.getString(
                                "data_abertura"
                        )
                ),
                dataEncerramento,
                tecnicoResponsavel,
                diagnostico,
                intervencoes
        );
    }

    private Equipamento buscarEquipamento(
            Connection connection,
            Long equipamentoId)
            throws SQLException {

        String sql = """
                SELECT *
                FROM equipamento
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    equipamentoId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "Equipamento relacionado à OS não encontrado."
                    );
                }

                return new Equipamento(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        rs.getString("tipo"),
                        rs.getString("identificador"),
                        StatusEquipamento.valueOf(
                                rs.getString("status")
                        )
                );
            }
        }
    }

    private Usuario buscarUsuario(
            Connection connection,
            Long usuarioId)
            throws SQLException {

        String sql = """
                SELECT *
                FROM usuario
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    usuarioId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "Usuário relacionado à OS não encontrado."
                    );
                }

                return new Usuario(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("login"),
                        PerfilUsuario.valueOf(
                                rs.getString("perfil")
                        )
                );
            }
        }
    }

    private Tecnico buscarTecnico(
            Connection connection,
            Long tecnicoId)
            throws SQLException {

        String sql = """
                SELECT
                    t.id AS tecnico_id,
                    t.disponibilidade,
                    u.id AS usuario_id,
                    u.nome,
                    u.login,
                    u.perfil
                FROM tecnico t
                INNER JOIN usuario u
                    ON u.id = t.usuario_id
                WHERE t.id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    tecnicoId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "Técnico relacionado à OS não encontrado."
                    );
                }

                Usuario usuario =
                        new Usuario(
                                rs.getLong(
                                        "usuario_id"
                                ),
                                rs.getString(
                                        "nome"
                                ),
                                rs.getString(
                                        "login"
                                ),
                                PerfilUsuario.valueOf(
                                        rs.getString(
                                                "perfil"
                                        )
                                )
                        );

                return new Tecnico(
                        rs.getLong(
                                "tecnico_id"
                        ),
                        usuario,
                        DisponibilidadeTecnico.valueOf(
                                rs.getString(
                                        "disponibilidade"
                                )
                        )
                );
            }
        }
    }

    private Diagnostico buscarDiagnostico(
            Connection connection,
            Long ordemServicoId)
            throws SQLException {

        String sql = """
                SELECT *
                FROM diagnostico
                WHERE ordem_servico_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    ordemServicoId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Tecnico tecnico =
                        buscarTecnico(
                                connection,
                                rs.getLong(
                                        "tecnico_id"
                                )
                        );

                return new Diagnostico(
                        rs.getString(
                                "parecer"
                        ),
                        rs.getString(
                                "causa_raiz"
                        ),
                        LocalDateTime.parse(
                                rs.getString(
                                        "data_hora"
                                )
                        ),
                        tecnico
                );
            }
        }
    }

    private List<Intervencao> buscarIntervencoes(
            Connection connection,
            Long ordemServicoId)
            throws SQLException {

        String sql = """
                SELECT *
                FROM intervencao
                WHERE ordem_servico_id = ?
                ORDER BY data_hora
                """;

        List<Intervencao> intervencoes =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    ordemServicoId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                while (rs.next()) {

                    Long intervencaoId =
                            rs.getLong("id");

                    Tecnico tecnico =
                            buscarTecnico(
                                    connection,
                                    rs.getLong(
                                            "tecnico_id"
                                    )
                            );

                    Intervencao intervencao =
                            new Intervencao(
                                    intervencaoId,
                                    LocalDateTime.parse(
                                            rs.getString(
                                                    "data_hora"
                                            )
                                    ),
                                    tecnico,
                                    rs.getString(
                                            "descricao"
                                    ),
                                    rs.getDouble(
                                            "horas_trabalhadas"
                                    )
                            );

                    List<MaterialUtilizado> materiais =
                            buscarMateriais(
                                    connection,
                                    intervencaoId
                            );

                    for (MaterialUtilizado material
                            : materiais) {

                        intervencao.adicionarMaterial(
                                material
                        );
                    }

                    intervencoes.add(
                            intervencao
                    );
                }
            }
        }

        return intervencoes;
    }

    private List<MaterialUtilizado> buscarMateriais(
            Connection connection,
            Long intervencaoId)
            throws SQLException {

        String sql = """
                SELECT *
                FROM material_utilizado
                WHERE intervencao_id = ?
                ORDER BY id
                """;

        List<MaterialUtilizado> materiais =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    intervencaoId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                while (rs.next()) {

                    MaterialUtilizado material =
                            new MaterialUtilizado(
                                    rs.getString(
                                            "descricao"
                                    ),
                                    rs.getDouble(
                                            "quantidade"
                                    ),
                                    rs.getString(
                                            "unidade"
                                    )
                            );

                    materiais.add(material);
                }
            }
        }

        return materiais;
    }
}