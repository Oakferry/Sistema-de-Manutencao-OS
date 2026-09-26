/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.application.service;

import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.repository.UsuarioRepository;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Usuario;

import java.util.List;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {

        if (usuarioRepository == null) {
            throw new IllegalArgumentException(
                    "UsuarioRepository é obrigatório."
            );
        }

        this.usuarioRepository = usuarioRepository;
    }

    public Usuario criarUsuario(
            String nome,
            String login,
            PerfilUsuario perfil) {

        boolean loginJaExiste =
                usuarioRepository
                        .buscarPorLogin(login)
                        .isPresent();

        if (loginJaExiste) {
            throw new DomainException(
                    "Já existe usuário com esse login."
            );
        }

        Usuario usuario = new Usuario(
                null,
                nome,
                login,
                perfil
        );

        return usuarioRepository.salvar(usuario);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository
                .buscarPorId(id)
                .orElseThrow(() ->
                        new DomainException(
                                "Usuário não encontrado."
                        )
                );
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.listarTodos();
    }

    public Usuario editarUsuario(
            Long id,
            String nome,
            String login) {

        Usuario usuario = buscarPorId(id);

        usuarioRepository
                .buscarPorLogin(login)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new DomainException(
                            "Já existe outro usuário com esse login."
                    );
                });

        usuario.atualizarDados(nome, login);

        return usuarioRepository.salvar(usuario);
    }

    public Usuario definirPerfil(
            Long id,
            PerfilUsuario perfil) {

        Usuario usuario = buscarPorId(id);

        usuario.alterarPerfil(perfil);

        return usuarioRepository.salvar(usuario);
    }
}
