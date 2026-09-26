/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package br.com.manutencao.domain.repository;

import br.com.manutencao.domain.equipamento.Equipamento;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author farin
 */
public interface EquipamentoRepository {
    Equipamento salvar(Equipamento equipamento);

    Optional<Equipamento> buscarPorId(Long id);

    Optional<Equipamento> buscarPorIdentificador(String identificador);

    List<Equipamento> listarTodos();

    void excluir(Long id);
}
