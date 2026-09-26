/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package br.com.manutencao.domain.repository;

/**
 *
 * @author farin
 */

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
}