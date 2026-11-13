package br.com.manutencao.infrastructure.persistence.sqlite;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

public final class ConnectionFactory {

    private static final Path DATA_DIRECTORY =
            Path.of("data");

    private static final String URL =
            "jdbc:sqlite:data/manutencao.db";

    private static final ThreadLocal<Connection> TRANSACAO_ATUAL =
            new ThreadLocal<>();

    private ConnectionFactory() {
    }

    public static boolean possuiTransacaoAtiva() {
        return TRANSACAO_ATUAL.get() != null;
    }

    public static Connection getConnection()
            throws SQLException {

        Connection compartilhada =
                TRANSACAO_ATUAL.get();

        if (compartilhada != null) {
            return emprestarConexao(compartilhada);
        }

        return abrirConexao();
    }

    public static <T> T executarEmTransacao(
            Supplier<T> operacao) {

        if (operacao == null) {
            throw new IllegalArgumentException(
                    "Operação da transação é obrigatória."
            );
        }

        if (possuiTransacaoAtiva()) {
            throw new IllegalStateException(
                    "Uma transação compartilhada já está ativa."
            );
        }

        try (Connection connection = abrirConexao()) {

            executarComando(
                    connection,
                    "BEGIN IMMEDIATE"
            );

            TRANSACAO_ATUAL.set(connection);

            try {

                T resultado = operacao.get();

                executarComando(
                        connection,
                        "COMMIT"
                );

                return resultado;

            } catch (RuntimeException | Error | SQLException e) {

                try {
                    executarComando(
                            connection,
                            "ROLLBACK"
                    );
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;

            } finally {
                TRANSACAO_ATUAL.remove();
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao executar transação no SQLite.",
                    e
            );
        }
    }

    private static Connection abrirConexao()
            throws SQLException {

        criarDiretorioDeDados();

        Connection connection =
                DriverManager.getConnection(URL);

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );

            statement.execute(
                    "PRAGMA busy_timeout = 5000"
            );

        } catch (SQLException e) {

            try {
                connection.close();
            } catch (SQLException closeException) {
                e.addSuppressed(closeException);
            }

            throw e;
        }

        return connection;
    }

    private static Connection emprestarConexao(
            Connection connection) {

        return (Connection) Proxy.newProxyInstance(
                ConnectionFactory.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {

                    // O repository fecha apenas o empréstimo.
                    // A conexão real pertence à transação.
                    if (method.getName().equals("close")) {
                        return null;
                    }

                    try {
                        return method.invoke(connection, args);

                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
        );
    }

    private static void executarComando(
            Connection connection,
            String sql) throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(sql);
        }
    }

    private static void criarDiretorioDeDados()
            throws SQLException {

        try {

            Files.createDirectories(
                    DATA_DIRECTORY
            );

        } catch (IOException e) {

            throw new SQLException(
                    "Não foi possível criar o diretório de dados.",
                    e
            );
        }
    }
}