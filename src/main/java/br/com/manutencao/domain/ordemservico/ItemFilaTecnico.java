package br.com.manutencao.domain.ordemservico;

import br.com.manutencao.domain.exception.DomainException;

import java.time.LocalDateTime;

public final class ItemFilaTecnico {

    private final Long id;
    private final Long ordemServicoId;
    private final Long tecnicoId;
    private final Long gestorId;
    private final LocalDateTime dataEntrada;

    // Criação de uma nova entrada na fila.
    public ItemFilaTecnico(
            Long ordemServicoId,
            Long tecnicoId,
            Long gestorId) {

        this(
                null,
                ordemServicoId,
                tecnicoId,
                gestorId,
                LocalDateTime.now()
        );
    }

    // Reconstrução de uma entrada salva no banco.
    public ItemFilaTecnico(
            Long id,
            Long ordemServicoId,
            Long tecnicoId,
            Long gestorId,
            LocalDateTime dataEntrada) {

        if (id != null && id <= 0) {
            throw new DomainException(
                    "ID da entrada na fila deve ser positivo."
            );
        }

        validarId(ordemServicoId, "Ordem de serviço");
        validarId(tecnicoId, "Técnico");
        validarId(gestorId, "Gestor");

        if (dataEntrada == null) {
            throw new DomainException(
                    "Data de entrada na fila é obrigatória."
            );
        }

        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.tecnicoId = tecnicoId;
        this.gestorId = gestorId;
        this.dataEntrada = dataEntrada;
    }

    private static void validarId(
            Long valor,
            String campo) {

        if (valor == null || valor <= 0) {
            throw new DomainException(
                    campo + " deve possuir um ID válido."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Long getOrdemServicoId() {
        return ordemServicoId;
    }

    public Long getTecnicoId() {
        return tecnicoId;
    }

    public Long getGestorId() {
        return gestorId;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }
}