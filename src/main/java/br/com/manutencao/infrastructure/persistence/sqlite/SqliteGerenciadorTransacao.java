package br.com.manutencao.infrastructure.persistence.sqlite;

import br.com.manutencao.application.transaction.GerenciadorTransacao;

import java.util.function.Supplier;

public class SqliteGerenciadorTransacao
        implements GerenciadorTransacao {

    @Override
    public <T> T executar(Supplier<T> operacao) {

        return ConnectionFactory.executarEmTransacao(
                operacao
        );
    }
}
