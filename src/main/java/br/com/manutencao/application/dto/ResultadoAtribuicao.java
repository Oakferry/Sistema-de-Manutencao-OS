package br.com.manutencao.application.dto;

import br.com.manutencao.domain.ordemservico.OrdemServico;

public record ResultadoAtribuicao(
        OrdemServico ordemServico,
        boolean enfileirada,
        Long ordemEmAtendimentoId,
        int posicaoFila) {
}