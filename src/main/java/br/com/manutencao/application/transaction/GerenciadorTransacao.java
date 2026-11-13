package br.com.manutencao.application.transaction;

import java.util.function.Supplier;

public interface GerenciadorTransacao {

    <T> T executar(Supplier<T> operacao);
}
