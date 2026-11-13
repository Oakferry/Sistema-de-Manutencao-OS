package br.com.manutencao.domain.repository;

import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.ordemservico.OrdemServico;
import br.com.manutencao.domain.ordemservico.StatusOrdemServico;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository {

    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(Long id);

    List<OrdemServico> listarTodos();

    List<OrdemServico> listarPorStatus(
            StatusOrdemServico status
    );

    List<OrdemServico> listarPorEquipamento(
            Long equipamentoId
    );

    List<OrdemServico> listarPorTecnico(
            Long tecnicoId
    );

    default Optional<OrdemServico> buscarAtivaPorTecnico(
            Long tecnicoId) {

        if (tecnicoId == null) {
            throw new DomainException(
                    "ID do técnico é obrigatório."
            );
        }

        List<OrdemServico> ordensAtivas =
                listarPorTecnico(tecnicoId)
                        .stream()
                        .filter(ordem ->
                                ordem.getStatus().ocupaTecnico()
                        )
                        .toList();

        if (ordensAtivas.size() > 1) {

            String ids = ordensAtivas
                    .stream()
                    .map(ordem ->
                            String.valueOf(ordem.getId())
                    )
                    .collect(
                            java.util.stream.Collectors.joining(", ")
                    );

            throw new DomainException(
                    "O técnico possui mais de uma OS ativa: "
                            + ids
                            + ". Regularize as atribuições "
                            + "antes de continuar."
            );
        }

        return ordensAtivas.stream().findFirst();
    }
}