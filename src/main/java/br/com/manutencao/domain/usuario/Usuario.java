package br.com.manutencao.domain.usuario;
import br.com.manutencao.domain.exception.DomainException;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author farin
 */
public class Usuario {
    private Long id;
    private String nome;
    private String login;
    private PerfilUsuario perfil;
    
    public Usuario(Long id, String nome, String login, PerfilUsuario perfil){
        validarDados(nome, login, perfil);
        
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.perfil = perfil;
    }
    
    public void atualizarDados(String nome, String login){
        validarTexto(nome, "Nome do usuário é obrigatório");
        validarTexto(login, "Login do usuário é obrigatório");
        
        this.nome = nome;
        this.login = login;
    }
    
    public void alterarPerfil(PerfilUsuario perfil){
        if(perfil == null){
            throw new DomainException("Perfil do usuário é obrigatório.");
        }
        
        this.perfil = perfil;
    }
    
    private void validarDados(String nome, String login, PerfilUsuario perfil){
        validarTexto(nome, "Nome do usuário é obrigatório");
        validarTexto(login, "Login do usuário é obrigatório");
        
        if(perfil == null){
            throw new DomainException("Perfil do usuário é obrigatório.");
        }
    }
    
    private void validarTexto(String valor, String mensagem){
        if(valor == null || valor.isBlank()){
            throw new DomainException(mensagem);
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public PerfilUsuario getPerfil(){
        return perfil;
    }    
    
}
