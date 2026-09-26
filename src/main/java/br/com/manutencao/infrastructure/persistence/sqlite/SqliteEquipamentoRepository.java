package br.com.manutencao.infrastructure.persistence.sqlite;

import br.com.manutencao.domain.equipamento.Equipamento;
import br.com.manutencao.domain.equipamento.StatusEquipamento;
import br.com.manutencao.domain.repository.EquipamentoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteEquipamentoRepository
        implements EquipamentoRepository {

    @Override
    public Equipamento salvar(Equipamento equipamento) {

        if (equipamento.getId() == null) {
            return inserir(equipamento);
        }

        return atualizar(equipamento);
    }

    private Equipamento inserir(Equipamento equipamento) {

        String sql = """
                INSERT INTO equipamento
                (nome, descricao, tipo, identificador, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(
                    1,
                    equipamento.getNome()
            );

            statement.setString(
                    2,
                    equipamento.getDescricao()
            );

            statement.setString(
                    3,
                    equipamento.getTipo()
            );

            statement.setString(
                    4,
                    equipamento.getIdentificador()
            );

            statement.setString(
                    5,
                    equipamento.getStatus().name()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {

                    Long id = generatedKeys.getLong(1);

                    return new Equipamento(
                            id,
                            equipamento.getNome(),
                            equipamento.getDescricao(),
                            equipamento.getTipo(),
                            equipamento.getIdentificador(),
                            equipamento.getStatus()
                    );
                }
            }

            throw new RuntimeException(
                    "Não foi possível obter o ID do equipamento."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao salvar equipamento.",
                    e
            );
        }
    }

    private Equipamento atualizar(Equipamento equipamento) {

        String sql = """
                UPDATE equipamento
                SET nome = ?,
                    descricao = ?,
                    tipo = ?,
                    identificador = ?,
                    status = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    equipamento.getNome()
            );

            statement.setString(
                    2,
                    equipamento.getDescricao()
            );

            statement.setString(
                    3,
                    equipamento.getTipo()
            );

            statement.setString(
                    4,
                    equipamento.getIdentificador()
            );

            statement.setString(
                    5,
                    equipamento.getStatus().name()
            );

            statement.setLong(
                    6,
                    equipamento.getId()
            );

            statement.executeUpdate();

            return equipamento;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar equipamento.",
                    e
            );
        }
    }

    @Override
    public Optional<Equipamento> buscarPorId(Long id) {

        String sql = """
                SELECT *
                FROM equipamento
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapearEquipamento(resultSet)
                    );
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar equipamento por ID.",
                    e
            );
        }
    }

    @Override
    public Optional<Equipamento> buscarPorIdentificador(
            String identificador) {

        String sql = """
                SELECT *
                FROM equipamento
                WHERE identificador = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    identificador
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapearEquipamento(resultSet)
                    );
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar equipamento por identificador.",
                    e
            );
        }
    }

    @Override
    public List<Equipamento> listarTodos() {

        String sql = """
                SELECT *
                FROM equipamento
                ORDER BY nome
                """;

        List<Equipamento> equipamentos =
                new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                equipamentos.add(
                        mapearEquipamento(resultSet)
                );
            }

            return equipamentos;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar equipamentos.",
                    e
            );
        }
    }

    @Override
    public void excluir(Long id) {

        String sql = """
                DELETE FROM equipamento
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao excluir equipamento.",
                    e
            );
        }
    }

    private Equipamento mapearEquipamento(
            ResultSet resultSet)
            throws SQLException {

        return new Equipamento(
                resultSet.getLong("id"),
                resultSet.getString("nome"),
                resultSet.getString("descricao"),
                resultSet.getString("tipo"),
                resultSet.getString("identificador"),
                StatusEquipamento.valueOf(
                        resultSet.getString("status")
                )
        );
    }
}
