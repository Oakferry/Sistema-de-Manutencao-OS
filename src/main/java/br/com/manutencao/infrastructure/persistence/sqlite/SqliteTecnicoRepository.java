package br.com.manutencao.infrastructure.persistence.sqlite;

import br.com.manutencao.domain.repository.TecnicoRepository;
import br.com.manutencao.domain.usuario.DisponibilidadeTecnico;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Tecnico;
import br.com.manutencao.domain.usuario.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTecnicoRepository
        implements TecnicoRepository {

    @Override
    public Tecnico salvar(Tecnico tecnico) {

        if (tecnico.getId() == null) {
            return inserir(tecnico);
        }

        return atualizar(tecnico);
    }

    private Tecnico inserir(Tecnico tecnico) {

        String sql = """
                INSERT INTO tecnico
                (usuario_id, disponibilidade)
                VALUES (?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    tecnico.getUsuario().getId()
            );

            statement.setString(
                    2,
                    tecnico.getDisponibilidade().name()
            );

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {

                    return new Tecnico(
                            keys.getLong(1),
                            tecnico.getUsuario(),
                            tecnico.getDisponibilidade()
                    );
                }
            }

            throw new RuntimeException(
                    "Não foi possível obter o ID do técnico."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao salvar técnico.",
                    e
            );
        }
    }

    private Tecnico atualizar(Tecnico tecnico) {

        String sql = """
                UPDATE tecnico
                SET usuario_id = ?,
                    disponibilidade = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    tecnico.getUsuario().getId()
            );

            statement.setString(
                    2,
                    tecnico.getDisponibilidade().name()
            );

            statement.setLong(
                    3,
                    tecnico.getId()
            );

            statement.executeUpdate();

            return tecnico;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar técnico.",
                    e
            );
        }
    }

    @Override
    public Optional<Tecnico> buscarPorId(Long id) {

        String sql = """
                SELECT
                    t.id AS tecnico_id,
                    t.disponibilidade,
                    u.id AS usuario_id,
                    u.nome,
                    u.login,
                    u.perfil
                FROM tecnico t
                JOIN usuario u
                    ON u.id = t.usuario_id
                WHERE t.id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapearTecnico(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar técnico por ID.",
                    e
            );
        }
    }

    @Override
    public Optional<Tecnico> buscarPorUsuarioId(
            Long usuarioId) {

        String sql = """
                SELECT
                    t.id AS tecnico_id,
                    t.disponibilidade,
                    u.id AS usuario_id,
                    u.nome,
                    u.login,
                    u.perfil
                FROM tecnico t
                JOIN usuario u
                    ON u.id = t.usuario_id
                WHERE u.id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, usuarioId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapearTecnico(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar técnico por usuário.",
                    e
            );
        }
    }

    @Override
    public List<Tecnico> listarTodos() {

        String sql = """
                SELECT
                    t.id AS tecnico_id,
                    t.disponibilidade,
                    u.id AS usuario_id,
                    u.nome,
                    u.login,
                    u.perfil
                FROM tecnico t
                JOIN usuario u
                    ON u.id = t.usuario_id
                ORDER BY u.nome
                """;

        return executarListagem(sql, null);
    }

    @Override
    public List<Tecnico> listarPorDisponibilidade(
            DisponibilidadeTecnico disponibilidade) {

        String sql = """
                SELECT
                    t.id AS tecnico_id,
                    t.disponibilidade,
                    u.id AS usuario_id,
                    u.nome,
                    u.login,
                    u.perfil
                FROM tecnico t
                JOIN usuario u
                    ON u.id = t.usuario_id
                WHERE t.disponibilidade = ?
                ORDER BY u.nome
                """;

        return executarListagem(
                sql,
                disponibilidade
        );
    }

    private List<Tecnico> executarListagem(
            String sql,
            DisponibilidadeTecnico disponibilidade) {

        List<Tecnico> tecnicos = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            if (disponibilidade != null) {
                statement.setString(
                        1,
                        disponibilidade.name()
                );
            }

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    tecnicos.add(mapearTecnico(rs));
                }
            }

            return tecnicos;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar técnicos.",
                    e
            );
        }
    }

    private Tecnico mapearTecnico(ResultSet rs)
            throws SQLException {

        Usuario usuario = new Usuario(
                rs.getLong("usuario_id"),
                rs.getString("nome"),
                rs.getString("login"),
                PerfilUsuario.valueOf(
                        rs.getString("perfil")
                )
        );

        return new Tecnico(
                rs.getLong("tecnico_id"),
                usuario,
                DisponibilidadeTecnico.valueOf(
                        rs.getString("disponibilidade")
                )
        );
    }
}