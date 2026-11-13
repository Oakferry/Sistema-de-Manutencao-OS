package br.com.manutencao.domain.ordemservico;

public enum StatusOrdemServico {

    ABERTA,
    ATRIBUIDA,
    EM_EXECUCAO,
    AGUARDANDO_PECA,
    REPARO_FINALIZADO,
    AGUARDANDO_APROVACAO,
    ENCERRADA,
    CANCELADA;

    public boolean ocupaTecnico() {
        return this == ATRIBUIDA
                || this == EM_EXECUCAO
                || this == AGUARDANDO_PECA;
    }
}