/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package br.com.manutencao.domain.repository;

/**
 *
 * @author farin
 */

import br.com.manutencao.domain.usuario.DisponibilidadeTecnico;
import br.com.manutencao.domain.usuario.Tecnico;
import java.util.List;
import java.util.Optional;

public interface TecnicoRepository {

    Tecnico salvar(Tecnico tecnico);

    Optional<Tecnico> buscarPorId(Long id);

    Optional<Tecnico> buscarPorUsuarioId(Long usuarioId);

    List<Tecnico> listarTodos();

    List<Tecnico> listarPorDisponibilidade(
            DisponibilidadeTecnico disponibilidade
    );
}
