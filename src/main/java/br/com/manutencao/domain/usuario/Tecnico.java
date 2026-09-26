/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.domain.usuario;
import br.com.manutencao.domain.exception.DomainException;
/**
 *
 * @author farin
 */
public class Tecnico {
    private Long id;
    private Usuario usuario;
    private DisponibilidadeTecnico disponibilidade;
    
    public Tecnico(Long id, Usuario usuario){
        if(usuario == null){
            throw new DomainException("Usuário do técnico é obrigatório.");
        }
        
                //um Tecnico só pode existir associado a um Usuario cujo perfil seja TECNICO.
        if(usuario.getPerfil() != PerfilUsuario.TECNICO){
            throw new DomainException(
                    "Somente usuário com perfil TÉCNICO pode possuir cadastro técnico."
            );
        }
        
        this.id = id;
        this.usuario = usuario;
        this.disponibilidade = DisponibilidadeTecnico.DISPONIVEL;
    }
    
    public Tecnico(
            Long id,
            Usuario usuario,
            DisponibilidadeTecnico disponibilidade){
    
        if(usuario == null){
            throw new DomainException("Usuário do técnico é obrigatório.");
        }
        
        //um Tecnico só pode existir associado a um Usuario cujo perfil seja TECNICO.
        if(usuario.getPerfil() != PerfilUsuario.TECNICO){
            throw new DomainException(
                    "Somente usuário com perfil TÉCNICO pode possuir cadastro técnico."
            );
        }
        
        if(disponibilidade == null){
            throw new DomainException(
                    "Disponibilidade do técnico é obrigatóri."
            );
        }
        
        this.id = id;
        this.usuario = usuario;
        this.disponibilidade = disponibilidade;
        
    }
    
    //os métodos comunicam melhor a intenção do domínio.
    public void ficarDisponivel(){
        this.disponibilidade = DisponibilidadeTecnico.DISPONIVEL;
    }    
    
    public void entrarEmAtendimento(){
        this.disponibilidade = DisponibilidadeTecnico.EM_ATENDIMENTO;
    }
    
    public void ficarAusente(){
        this.disponibilidade = DisponibilidadeTecnico.AUSENTE;
    }
    
    public Long getId(){
        return id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }

    public DisponibilidadeTecnico getDisponibilidade() {
        return disponibilidade;
    }
}
