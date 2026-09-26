/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.domain.ordemservico;

/**
 *
 * @author farin
 */
import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.usuario.Tecnico;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Intervencao {

    private Long id;
    private final LocalDateTime dataHora;
    private final Tecnico tecnico;
    private final String descricao;
    private final double horasTrabalhadas;
    private final List<MaterialUtilizado> materiais;

    public Intervencao(
            Long id,
            LocalDateTime dataHora,
            Tecnico tecnico,
            String descricao,
            double horasTrabalhadas) {

        if (dataHora == null) {
            throw new DomainException(
                    "Data e hora da intervenção são obrigatórias."
            );
        }

        if (tecnico == null) {
            throw new DomainException(
                    "Técnico da intervenção é obrigatório."
            );
        }

        if (descricao == null || descricao.isBlank()) {
            throw new DomainException(
                    "Descrição da intervenção é obrigatória."
            );
        }

        if (horasTrabalhadas <= 0) {
            throw new DomainException(
                    "Horas trabalhadas devem ser maiores que zero."
            );
        }

        this.id = id;
        this.dataHora = dataHora;
        this.tecnico = tecnico;
        this.descricao = descricao;
        this.horasTrabalhadas = horasTrabalhadas;
        this.materiais = new ArrayList<>();
    }

    public void adicionarMaterial(MaterialUtilizado material) {
        if (material == null) {
            throw new DomainException(
                    "Material não pode ser nulo."
            );
        }

        materiais.add(material);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getHorasTrabalhadas() {
        return horasTrabalhadas;
    }

    public List<MaterialUtilizado> getMateriais() {
        return Collections.unmodifiableList(materiais);
    }
}
