package br.com.manutencao.infrastructure.persistence.sqlite;

import br.com.manutencao.domain.ordemservico.ItemFilaTecnico;
import br.com.manutencao.domain.repository.FilaTecnicoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteFilaTecnicoRepository
        implements FilaTecnicoRepository {

    @Override
    public ItemFilaTecnico salvar(ItemFilaTecnico item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Entrada na fila é obrigatória."
            );
        }

        if (item.getId() != null) {
            throw new IllegalArgumentException(
                    "Esta operação aceita somente novas entradas na fila."
            );
        }

        String sql = """
                INSERT INTO fila_tecnico (
                    ordem_servico_id,
                    tecnico_id,
                    gestor_id,
                    data_entrada
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection =
                     ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    item.getOrdemServicoId()
            );

            statement.setLong(
                    2,
                    item.getTecnicoId()
            );

            statement.setLong(
                    3,
                    item.getGestorId()
            );

            statement.setString(
                    4,
                    item.getDataEntrada().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Não foi possível obter o ID "
                                    + "da entrada na fila."
                    );
                }

                return new ItemFilaTecnico(
                        keys.getLong(1),
                        item.getOrdemServicoId(),
                        item.getTecnicoId(),
                        item.getGestorId(),
                        item.getDataEntrada()
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao salvar entrada na fila do técnico.",
                    e
            );
        }
    }

    @Override
    public Optional<ItemFilaTecnico> buscarPorOrdemServico(
            Long ordemServicoId) {

        String sql = """
                SELECT *
                FROM fila_tecnico
                WHERE ordem_servico_id = ?
                """;

        return consultar(sql, ordemServicoId)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ItemFilaTecnico> buscarPrimeiroPorTecnico(
            Long tecnicoId) {

        String sql = """
                SELECT *
                FROM fila_tecnico
                WHERE tecnico_id = ?
                ORDER BY id ASC
                LIMIT 1
                """;

        return consultar(sql, tecnicoId)
                .stream()
                .findFirst();
    }

    @Override
    public List<ItemFilaTecnico> listarPorTecnico(
            Long tecnicoId) {

        String sql = """
                SELECT *
                FROM fila_tecnico
                WHERE tecnico_id = ?
                ORDER BY id ASC
                """;

        return consultar(sql, tecnicoId);
    }

    @Override
    public void removerPorOrdemServico(
            Long ordemServicoId) {

        validarId(ordemServicoId);

        String sql = """
                DELETE FROM fila_tecnico
                WHERE ordem_servico_id = ?
                """;

        try (Connection connection =
                     ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, ordemServicoId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao remover ordem de serviço da fila.",
                    e
            );
        }
    }

    private List<ItemFilaTecnico> consultar(
            String sql,
            Long parametro) {

        validarId(parametro);

        List<ItemFilaTecnico> itens =
                new ArrayList<>();

        try (Connection connection =
                     ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, parametro);

            try (ResultSet rs =
                         statement.executeQuery()) {

                while (rs.next()) {
                    itens.add(mapear(rs));
                }
            }

            return itens;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao consultar fila do técnico.",
                    e
            );
        }
    }

    private ItemFilaTecnico mapear(
            ResultSet rs) throws SQLException {

        return new ItemFilaTecnico(
                rs.getLong("id"),
                rs.getLong("ordem_servico_id"),
                rs.getLong("tecnico_id"),
                rs.getLong("gestor_id"),
                LocalDateTime.parse(
                        rs.getString("data_entrada")
                )
        );
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "O ID informado deve ser positivo."
            );
        }
    }
}
