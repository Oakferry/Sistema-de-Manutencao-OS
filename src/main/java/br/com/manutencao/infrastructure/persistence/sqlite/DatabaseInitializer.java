package br.com.manutencao.infrastructure.persistence.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final Path SCHEMA_PATH =
            Path.of("scripts", "schema.sql");

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {

        String script = carregarSchema();

        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(
                    "PRAGMA foreign_keys = ON"
            );

            String[] comandos = script.split(";");

            for (String comando : comandos) {

                String sql = comando.trim();

                if (!sql.isEmpty()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private static String carregarSchema()
            throws SQLException {

        try {
            return Files.readString(SCHEMA_PATH);
        } catch (IOException e) {
            throw new SQLException(
                    "Não foi possível carregar o arquivo schema.sql.",
                    e
            );
        }
    }
}
