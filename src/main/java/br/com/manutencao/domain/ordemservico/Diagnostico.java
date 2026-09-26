/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.domain.ordemservico;

import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.usuario.Tecnico;
import java.time.LocalDateTime;
/**
 *
 * @author farin
 */

public class Diagnostico {

    private final String parecer;
    private final String causaRaiz;
    private final LocalDateTime dataHora;
    private final Tecnico tecnico;

    public Diagnostico(
            String parecer,
            String causaRaiz,
            LocalDateTime dataHora,
            Tecnico tecnico) {

        if (parecer == null || parecer.isBlank()) {
            throw new DomainException(
                    "Parecer técnico é obrigatório."
            );
        }

        if (causaRaiz == null || causaRaiz.isBlank()) {
            throw new DomainException(
                    "Causa raiz é obrigatória."
            );
        }

        if (dataHora == null) {
            throw new DomainException(
                    "Data e hora do diagnóstico são obrigatórias."
            );
        }

        if (tecnico == null) {
            throw new DomainException(
                    "Técnico do diagnóstico é obrigatório."
            );
        }

        this.parecer = parecer;
        this.causaRaiz = causaRaiz;
        this.dataHora = dataHora;
        this.tecnico = tecnico;
    }

    public String getParecer() {
        return parecer;
    }

    public String getCausaRaiz() {
        return causaRaiz;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }
}
