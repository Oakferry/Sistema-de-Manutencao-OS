/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.manutencao.domain.ordemservico;

import br.com.manutencao.domain.equipamento.Equipamento;
import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.usuario.Tecnico;
import br.com.manutencao.domain.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Caio FARINHA
 */

public class OrdemServico {

    private Long id;
    private Equipamento equipamento;
    private Usuario usuarioAbertura;
    private String descricaoProblema;
    private Criticidade criticidade;
    private StatusOrdemServico status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;

    private Tecnico tecnicoResponsavel;
    private Diagnostico diagnostico;

    private final List<Intervencao> intervencoes;

    public OrdemServico(
            Long id,
            Equipamento equipamento,
            Usuario usuarioAbertura,
            String descricaoProblema,
            Criticidade criticidade) {

        if (equipamento == null) {
            throw new DomainException(
                    "Equipamento da ordem de serviço é obrigatório."
            );
        }

        if (usuarioAbertura == null) {
            throw new DomainException(
                    "Usuário responsável pela abertura é obrigatório."
            );
        }

        if (descricaoProblema == null || descricaoProblema.isBlank()) {
            throw new DomainException(
                    "Descrição do problema é obrigatória."
            );
        }

        if (criticidade == null) {
            throw new DomainException(
                    "Criticidade da ordem de serviço é obrigatória."
            );
        }

        this.id = id;
        this.equipamento = equipamento;
        this.usuarioAbertura = usuarioAbertura;
        this.descricaoProblema = descricaoProblema;
        this.criticidade = criticidade;

        this.status = StatusOrdemServico.ABERTA;
        this.dataAbertura = LocalDateTime.now();

        this.intervencoes = new ArrayList<>();
    }

    public void atribuirTecnico(Tecnico tecnico) {

        if (tecnico == null) {
            throw new DomainException(
                    "Técnico responsável é obrigatório."
            );
        }

        if (status != StatusOrdemServico.ABERTA) {
            throw new DomainException(
                    "Somente ordem de serviço aberta pode receber atribuição inicial."
            );
        }

        this.tecnicoResponsavel = tecnico;
        this.status = StatusOrdemServico.ATRIBUIDA;
    }

    public void reatribuirTecnico(Tecnico novoTecnico) {

        if (novoTecnico == null) {
            throw new DomainException(
                    "Novo técnico responsável é obrigatório."
            );
        }

        if (status == StatusOrdemServico.ENCERRADA
                || status == StatusOrdemServico.CANCELADA) {

            throw new DomainException(
                    "Ordem de serviço encerrada ou cancelada não pode ser reatribuída."
            );
        }

        this.tecnicoResponsavel = novoTecnico;
    }

    public void registrarDiagnostico(Diagnostico diagnostico) {

        if (diagnostico == null) {
            throw new DomainException(
                    "Diagnóstico não pode ser nulo."
            );
        }

        if (status != StatusOrdemServico.ATRIBUIDA) {
            throw new DomainException(
                    "Diagnóstico só pode ser registrado em ordem atribuída."
            );
        }

        validarTecnicoResponsavel(diagnostico.getTecnico());

        this.diagnostico = diagnostico;
    }

    public void iniciarExecucao(Tecnico tecnico) {

        validarTecnicoResponsavel(tecnico);

        if (status != StatusOrdemServico.ATRIBUIDA) {
            throw new DomainException(
                    "A ordem de serviço não está pronta para iniciar execução."
            );
        }

        if (diagnostico == null) {
            throw new DomainException(
                    "A ordem de serviço precisa possuir diagnóstico antes da execução."
            );
        }

        this.status = StatusOrdemServico.EM_EXECUCAO;
    }

    public void registrarIntervencao(Intervencao intervencao) {

        if (intervencao == null) {
            throw new DomainException(
                    "Intervenção não pode ser nula."
            );
        }

        if (status != StatusOrdemServico.EM_EXECUCAO) {
            throw new DomainException(
                    "Intervenções só podem ser registradas durante a execução."
            );
        }

        validarTecnicoResponsavel(intervencao.getTecnico());

        intervencoes.add(intervencao);
    }

    public void aguardarPeca(Tecnico tecnico) {

        validarTecnicoResponsavel(tecnico);

        if (status != StatusOrdemServico.EM_EXECUCAO) {
            throw new DomainException(
                    "Somente ordem em execução pode aguardar peça."
            );
        }

        this.status = StatusOrdemServico.AGUARDANDO_PECA;
    }

    public void retomarExecucao(Tecnico tecnico) {

        validarTecnicoResponsavel(tecnico);

        if (status != StatusOrdemServico.AGUARDANDO_PECA) {
            throw new DomainException(
                    "Somente ordem aguardando peça pode retomar execução."
            );
        }

        this.status = StatusOrdemServico.EM_EXECUCAO;
    }

    public void finalizarReparo(Tecnico tecnico) {

        validarTecnicoResponsavel(tecnico);

        if (status != StatusOrdemServico.EM_EXECUCAO) {
            throw new DomainException(
                    "Somente ordem em execução pode finalizar o reparo."
            );
        }

        if (intervencoes.isEmpty()) {
            throw new DomainException(
                    "Não é possível finalizar reparo sem intervenção registrada."
            );
        }

        this.status = StatusOrdemServico.REPARO_FINALIZADO;
    }

    public void submeterParaAprovacao(Tecnico tecnico) {

        validarTecnicoResponsavel(tecnico);

        if (status != StatusOrdemServico.REPARO_FINALIZADO) {
            throw new DomainException(
                    "Somente reparo finalizado pode ser submetido para aprovação."
            );
        }

        this.status = StatusOrdemServico.AGUARDANDO_APROVACAO;
    }

    public void encerrar() {

        if (status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new DomainException(
                    "Somente ordem aguardando aprovação pode ser encerrada."
            );
        }

        this.status = StatusOrdemServico.ENCERRADA;
        this.dataEncerramento = LocalDateTime.now();
    }

    public void cancelar() {

        if (status == StatusOrdemServico.ENCERRADA) {
            throw new DomainException(
                    "Ordem de serviço encerrada não pode ser cancelada."
            );
        }

        if (status == StatusOrdemServico.CANCELADA) {
            throw new DomainException(
                    "Ordem de serviço já está cancelada."
            );
        }

        this.status = StatusOrdemServico.CANCELADA;
    }

    private void validarTecnicoResponsavel(Tecnico tecnico) {

        if (tecnico == null) {
            throw new DomainException(
                    "Técnico é obrigatório para esta operação."
            );
        }

        if (tecnicoResponsavel == null) {
            throw new DomainException(
                    "A ordem de serviço não possui técnico responsável."
            );
        }

        if (!mesmoTecnico(tecnicoResponsavel, tecnico)) {
            throw new DomainException(
                    "Somente o técnico responsável pode executar esta operação."
            );
        }
    }

    private boolean mesmoTecnico(Tecnico primeiro, Tecnico segundo) {

        if (primeiro == segundo) {
            return true;
        }

        if (primeiro.getId() == null || segundo.getId() == null) {
            return false;
        }

        return primeiro.getId().equals(segundo.getId());
    }

    public Long getId() {
        return id;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public Usuario getUsuarioAbertura() {
        return usuarioAbertura;
    }

    public String getDescricaoProblema() {
        return descricaoProblema;
    }

    public Criticidade getCriticidade() {
        return criticidade;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public LocalDateTime getDataEncerramento() {
        return dataEncerramento;
    }

    public Tecnico getTecnicoResponsavel() {
        return tecnicoResponsavel;
    }

    public Diagnostico getDiagnostico() {
        return diagnostico;
    }

    public List<Intervencao> getIntervencoes() {
        return Collections.unmodifiableList(intervencoes);
    }
      
}
