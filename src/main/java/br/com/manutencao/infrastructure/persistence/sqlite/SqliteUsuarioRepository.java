package br.com.manutencao.infrastructure.persistence.sqlite;

import br.com.manutencao.domain.repository.UsuarioRepository;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteUsuarioRepository
        implements UsuarioRepository {

    @Override
    public Usuario salvar(Usuario usuario) {

        if (usuario.getId() == null) {
            return inserir(usuario);
        }

        return atualizar(usuario);
    }

    private Usuario inserir(Usuario usuario) {

        String sql = """
                INSERT INTO usuario
                (nome, login, perfil)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getLogin());
            statement.setString(3, usuario.getPerfil().name());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {

                    return new Usuario(
                            keys.getLong(1),
                            usuario.getNome(),
                            usuario.getLogin(),
                            usuario.getPerfil()
                    );
                }
            }

            throw new RuntimeException(
                    "Não foi possível obter o ID do usuário."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao salvar usuário.",
                    e
            );
        }
    }

    private Usuario atualizar(Usuario usuario) {

        String sql = """
                UPDATE usuario
                SET nome = ?,
                    login = ?,
                    perfil = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getLogin());
            statement.setString(3, usuario.getPerfil().name());
            statement.setLong(4, usuario.getId());

            statement.executeUpdate();

            return usuario;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar usuário.",
                    e
            );
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {

        String sql = """
                SELECT *
                FROM usuario
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar usuário por ID.",
                    e
            );
        }
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {

        String sql = """
                SELECT *
                FROM usuario
                WHERE login = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, login);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar usuário por login.",
                    e
            );
        }
    }

    @Override
    public List<Usuario> listarTodos() {

        String sql = """
                SELECT *
                FROM usuario
                ORDER BY nome
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }

            return usuarios;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar usuários.",
                    e
            );
        }
    }

    private Usuario mapearUsuario(ResultSet rs)
            throws SQLException {

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