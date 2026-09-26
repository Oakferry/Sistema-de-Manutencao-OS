/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.application.service;

import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.repository.TecnicoRepository;
import br.com.manutencao.domain.repository.UsuarioRepository;
import br.com.manutencao.domain.usuario.DisponibilidadeTecnico;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Tecnico;
import br.com.manutencao.domain.usuario.Usuario;

import java.util.List;

public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;

    public TecnicoService(
            TecnicoRepository tecnicoRepository,
            UsuarioRepository usuarioRepository) {

        if (tecnicoRepository == null) {
            throw new IllegalArgumentException(
                    "TecnicoRepository é obrigatório."
            );
        }

        if (usuarioRepository == null) {
            throw new IllegalArgumentException(
                    "UsuarioRepository é obrigatório."
            );
        }

        this.tecnicoRepository = tecnicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Tecnico criarTecnico(Long usuarioId) {

        Usuario usuario = usuarioRepository
                .buscarPorId(usuarioId)
                .orElseThrow(() ->
                        new DomainException(
                                "Usuário não encontrado."
                        )
                );

        if (usuario.getPerfil() != PerfilUsuario.TECNICO) {
            throw new DomainException(
                    "O usuário precisa possuir perfil TÉCNICO."
            );
        }

        boolean jaPossuiCadastroTecnico =
                tecnicoRepository
                        .buscarPorUsuarioId(usuarioId)
                        .isPresent();

        if (jaPossuiCadastroTecnico) {
            throw new DomainException(
                    "Este usuário já possui cadastro técnico."
            );
        }

        Tecnico tecnico = new Tecnico(
                null,
                usuario
        );

        return tecnicoRepository.salvar(tecnico);
    }

    public Tecnico buscarPorId(Long id) {
        return tecnicoRepository
                .buscarPorId(id)
                .orElseThrow(() ->
                        new DomainException(
                                "Técnico não encontrado."
                        )
                );
    }

    public List<Tecnico> listarTodos() {
        return tecnicoRepository.listarTodos();
    }

    public List<Tecnico> listarDisponiveis() {
        return tecnicoRepository
                .listarPorDisponibilidade(
                        DisponibilidadeTecnico.DISPONIVEL
                );
    }

    public Tecnico ficarDisponivel(Long tecnicoId) {

        Tecnico tecnico = buscarPorId(tecnicoId);

        tecnico.ficarDisponivel();

        return tecnicoRepository.salvar(tecnico);
    }

    public Tecnico entrarEmAtendimento(Long tecnicoId) {

        Tecnico tecnico = buscarPorId(tecnicoId);

        tecnico.entrarEmAtendimento();

        return tecnicoRepository.salvar(tecnico);
    }

    public Tecnico ficarAusente(Long tecnicoId) {

        Tecnico tecnico = buscarPorId(tecnicoId);

        tecnico.ficarAusente();

        return tecnicoRepository.salvar(tecnico);
    }
}
