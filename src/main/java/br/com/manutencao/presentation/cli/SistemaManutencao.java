package br.com.manutencao.presentation.cli;

import br.com.manutencao.infrastructure.persistence.sqlite.DatabaseInitializer;
import java.sql.SQLException;

public class SistemaManutencao {

    public static void main(String[] args) {

        try {

            DatabaseInitializer.initialize();

            System.out.println(
                    "Banco de dados inicializado com sucesso."
            );

        } catch (SQLException e) {

            System.out.println(
                    "Erro ao inicializar banco de dados: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}