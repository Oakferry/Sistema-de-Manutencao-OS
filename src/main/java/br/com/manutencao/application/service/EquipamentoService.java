/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.application.service;

import br.com.manutencao.domain.equipamento.Equipamento;
import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.repository.EquipamentoRepository;
import br.com.manutencao.domain.repository.OrdemServicoRepository;

import java.util.List;

public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public EquipamentoService(
            EquipamentoRepository equipamentoRepository,
            OrdemServicoRepository ordemServicoRepository) {

        if (equipamentoRepository == null) {
            throw new IllegalArgumentException(
                    "EquipamentoRepository é obrigatório."
            );
        }

        if (ordemServicoRepository == null) {
            throw new IllegalArgumentException(
                    "OrdemServicoRepository é obrigatório."
            );
        }

        this.equipamentoRepository = equipamentoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    public Equipamento cadastrar(
            String nome,
            String descricao,
            String tipo,
            String identificador) {

        boolean identificadorJaExiste =
                equipamentoRepository
                        .buscarPorIdentificador(identificador)
                        .isPresent();

        if (identificadorJaExiste) {
            throw new DomainException(
                    "Já existe equipamento com esse identificador."
            );
        }

        Equipamento equipamento = new Equipamento(
                null,
                nome,
                descricao,
                tipo,
                identificador
        );

        return equipamentoRepository.salvar(equipamento);
    }

    public Equipamento buscarPorId(Long id) {
        return equipamentoRepository
                .buscarPorId(id)
                .orElseThrow(() ->
                        new DomainException(
                                "Equipamento não encontrado."
                        )
                );
    }

    public List<Equipamento> listarTodos() {
        return equipamentoRepository.listarTodos();
    }

    public Equipamento editar(
            Long id,
            String nome,
            String descricao,
            String tipo,
            String identificador) {

        Equipamento equipamento = buscarPorId(id);

        equipamentoRepository
                .buscarPorIdentificador(identificador)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new DomainException(
                            "Já existe outro equipamento com esse identificador."
                    );
                });

        equipamento.atualizarDados(
                nome,
                descricao,
                tipo,
                identificador
        );

        return equipamentoRepository.salvar(equipamento);
    }

    public Equipamento inativar(Long id) {
        Equipamento equipamento = buscarPorId(id);

        equipamento.inativar();

        return equipamentoRepository.salvar(equipamento);
    }

    public Equipamento ativar(Long id) {
        Equipamento equipamento = buscarPorId(id);

        equipamento.ativar();

        return equipamentoRepository.salvar(equipamento);
    }

    public void excluir(Long id) {
        Equipamento equipamento = buscarPorId(id);

        boolean possuiHistorico =
                !ordemServicoRepository
                        .listarPorEquipamento(equipamento.getId())
                        .isEmpty();

        if (possuiHistorico) {
            throw new DomainException(
                    "Equipamento com histórico de ordens de serviço não pode ser excluído."
            );
        }

        equipamentoRepository.excluir(id);
    }
}
