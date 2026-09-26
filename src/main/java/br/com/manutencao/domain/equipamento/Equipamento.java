/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.domain.equipamento;

/**
 *
 * @author farin
 */

public class Equipamento {

    private Long id;
    private String nome;
    private String descricao;
    private String tipo;
    private String identificador;
    private StatusEquipamento status;

    public Equipamento(
            Long id,
            String nome,
            String descricao,
            String tipo,
            String identificador) {

        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.identificador = identificador;
        this.status = StatusEquipamento.ATIVO;
    }

    public Equipamento(
            Long id,
            String nome,
            String descricao,
            String tipo,
            String identificador,
            StatusEquipamento status) {

        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.identificador = identificador;
        this.status = status;
    }

    public void atualizarDados(
            String nome,
            String descricao,
            String tipo,
            String identificador) {

        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.identificador = identificador;
    }

    public void ativar() {
        this.status = StatusEquipamento.ATIVO;
    }

    public void inativar() {
        this.status = StatusEquipamento.INATIVO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public String getIdentificador() {
        return identificador;
    }

    public StatusEquipamento getStatus() {
        return status;
    }
}
