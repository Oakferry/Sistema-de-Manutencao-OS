package br.com.manutencao.infrastructure.persistence.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class ConnectionFactory {

    private static final Path DATA_DIRECTORY =
            Path.of("data");

    private static final String URL =
            "jdbc:sqlite:data/manutencao.db";

    private ConnectionFactory() {
    }

    public static Connection getConnection()
            throws SQLException {

        criarDiretorioDeDados();

        Connection connection =
                DriverManager.getConnection(URL);

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );
        }

        return connection;
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