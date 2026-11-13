package br.com.manutencao.domain.repository;

import br.com.manutencao.domain.ordemservico.ItemFilaTecnico;

import java.util.List;
import java.util.Optional;

public interface FilaTecnicoRepository {

    ItemFilaTecnico salvar(ItemFilaTecnico item);

    Optional<ItemFilaTecnico> buscarPorOrdemServico(
            Long ordemServicoId
    );

    // Retorna a entrada mais antiga ainda na fila.
    Optional<ItemFilaTecnico> buscarPrimeiroPorTecnico(
            Long tecnicoId
    );

    // Retorna a fila na ordem de entrada.
    List<ItemFilaTecnico> listarPorTecnico(
            Long tecnicoId
    );

    void removerPorOrdemServico(
            Long ordemServicoId
    );
}
