/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.domain.ordemservico;
import br.com.manutencao.domain.exception.DomainException;
/**
 *
 * @author farin
 */
public class MaterialUtilizado {
    private final String descricao;
    private final double quantidade;
    private final String unidade;
    
    public MaterialUtilizado(
            String descricao,
            double quantidade,
            String unidade){
        
        if(descricao == null || descricao.isBlank()){
            throw new DomainException("Descricao do material é obrigatória");
        }
        
        if(quantidade <= 0){
            throw new DomainException("Quantidade do material deve ser maior que zero");
        }
        
        if(unidade == null || unidade.isBlank()){
            throw new DomainException("Unidade do material é obrigatória.");
        }
        
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.unidade = unidade;
        
    }
    
    public String getDescricao() {
        return descricao;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }
}
