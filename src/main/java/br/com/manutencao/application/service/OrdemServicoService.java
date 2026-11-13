package br.com.manutencao.application.service;

import br.com.manutencao.application.dto.ResultadoAtribuicao;
import br.com.manutencao.application.transaction.GerenciadorTransacao;
import br.com.manutencao.domain.equipamento.Equipamento;
import br.com.manutencao.domain.equipamento.StatusEquipamento;
import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.ordemservico.Criticidade;
import br.com.manutencao.domain.ordemservico.Diagnostico;
import br.com.manutencao.domain.ordemservico.Intervencao;
import br.com.manutencao.domain.ordemservico.ItemFilaTecnico;
import br.com.manutencao.domain.ordemservico.MaterialUtilizado;
import br.com.manutencao.domain.ordemservico.OrdemServico;
import br.com.manutencao.domain.ordemservico.StatusOrdemServico;
import br.com.manutencao.domain.repository.EquipamentoRepository;
import br.com.manutencao.domain.repository.FilaTecnicoRepository;
import br.com.manutencao.domain.repository.OrdemServicoRepository;
import br.com.manutencao.domain.repository.TecnicoRepository;
import br.com.manutencao.domain.repository.UsuarioRepository;
import br.com.manutencao.domain.usuario.DisponibilidadeTecnico;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Tecnico;
import br.com.manutencao.domain.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final FilaTecnicoRepository filaTecnicoRepository;
    private final GerenciadorTransacao gerenciadorTransacao;

    public OrdemServicoService(
            OrdemServicoRepository ordemServicoRepository,
            EquipamentoRepository equipamentoRepository,
            UsuarioRepository usuarioRepository,
            TecnicoRepository tecnicoRepository,
            FilaTecnicoRepository filaTecnicoRepository,
            GerenciadorTransacao gerenciadorTransacao) {

        this.ordemServicoRepository = Objects.requireNonNull(
                ordemServicoRepository, "OrdemServicoRepository é obrigatório."
        );
        this.equipamentoRepository = Objects.requireNonNull(
                equipamentoRepository, "EquipamentoRepository é obrigatório."
        );
        this.usuarioRepository = Objects.requireNonNull(
                usuarioRepository, "UsuarioRepository é obrigatório."
        );
        this.tecnicoRepository = Objects.requireNonNull(
                tecnicoRepository, "TecnicoRepository é obrigatório."
        );
        this.filaTecnicoRepository = Objects.requireNonNull(
                filaTecnicoRepository, "FilaTecnicoRepository é obrigatório."
        );
        this.gerenciadorTransacao = Objects.requireNonNull(
                gerenciadorTransacao, "GerenciadorTransacao é obrigatório."
        );
    }

    public OrdemServico abrirOrdemServico(
            Long equipamentoId,
            Long usuarioAberturaId,
            String descricaoProblema,
            Criticidade criticidade) {

        Equipamento equipamento = equipamentoRepository
                .buscarPorId(equipamentoId)
                .orElseThrow(() ->
                        new DomainException("Equipamento não encontrado.")
                );

        if (equipamento.getStatus() != StatusEquipamento.ATIVO) {
            throw new DomainException(
                    "Não é possível abrir uma ordem de serviço "
                            + "para equipamento inativo."
            );
        }

        Usuario usuario = usuarioRepository
                .buscarPorId(usuarioAberturaId)
                .orElseThrow(() ->
                        new DomainException("Usuário não encontrado.")
                );

        OrdemServico ordemServico = new OrdemServico(
                null,
                equipamento,
                usuario,
                descricaoProblema,
                criticidade
        );

        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico buscarPorId(Long id) {
        return ordemServicoRepository
                .buscarPorId(id)
                .orElseThrow(() ->
                        new DomainException("Ordem de serviço não encontrada.")
                );
    }

    public List<OrdemServico> listarTodos() {
        return ordemServicoRepository.listarTodos();
    }

    public List<OrdemServico> listarPorStatus(StatusOrdemServico status) {
        return ordemServicoRepository.listarPorStatus(status);
    }

    public ResultadoAtribuicao atribuirTecnico(
            Long ordemServicoId,
            Long gestorId,
            Long tecnicoId) {

        return gerenciadorTransacao.executar(() -> {
            buscarUsuarioGestor(gestorId);

            Tecnico tecnico = buscarTecnico(tecnicoId);

            if (tecnico.getDisponibilidade() == DisponibilidadeTecnico.AUSENTE) {
                throw new DomainException(
                        "Técnico ausente não pode receber ordem de serviço."
                );
            }

            OrdemServico ordemServico = buscarPorId(ordemServicoId);

            if (ordemServico.getStatus() != StatusOrdemServico.ABERTA
                    || ordemServico.getTecnicoResponsavel() != null) {
                throw new DomainException(
                        "Somente uma OS aberta e sem técnico responsável "
                                + "pode receber atribuição inicial."
                );
            }

            Optional<ItemFilaTecnico> entradaExistente =
                    filaTecnicoRepository.buscarPorOrdemServico(ordemServicoId);

            if (entradaExistente.isPresent()) {
                throw new DomainException(
                        "A OS já está na fila do técnico ID "
                                + entradaExistente.get().getTecnicoId()
                                + "."
                );
            }

            Optional<OrdemServico> ordemAtiva =
                    ordemServicoRepository.buscarAtivaPorTecnico(tecnicoId);

            if (ordemAtiva.isPresent()) {
                ItemFilaTecnico entrada = new ItemFilaTecnico(
                        ordemServicoId, tecnicoId, gestorId
                );

                filaTecnicoRepository.salvar(entrada);

                tecnico.entrarEmAtendimento();
                tecnicoRepository.salvar(tecnico);

                int posicao = filaTecnicoRepository
                        .listarPorTecnico(tecnicoId)
                        .size();

                return new ResultadoAtribuicao(
                        ordemServico, true, ordemAtiva.get().getId(), posicao
                );
            }

            // Impede uma nova OS de ultrapassar entradas anteriores.
            if (filaTecnicoRepository.buscarPrimeiroPorTecnico(tecnicoId).isPresent()) {
                throw new DomainException(
                        "O técnico possui uma fila pendente. "
                                + "A primeira OS da fila deve ser "
                                + "encaminhada antes de uma nova atribuição."
                );
            }

            ordemServico.atribuirTecnico(tecnico);
            OrdemServico persistida = ordemServicoRepository.salvar(ordemServico);

            tecnico.entrarEmAtendimento();
            tecnicoRepository.salvar(tecnico);

            return new ResultadoAtribuicao(persistida, false, null, 0);
        });
    }

    public OrdemServico reatribuirTecnico(
            Long ordemServicoId,
            Long gestorId,
            Long novoTecnicoId) {

        return gerenciadorTransacao.executar(() -> {
            buscarUsuarioGestor(gestorId);

            OrdemServico ordemServico = buscarPorId(ordemServicoId);

            if (!ordemServico.getStatus().ocupaTecnico()) {
                throw new DomainException(
                        "Somente uma OS atribuída, em execução "
                                + "ou aguardando peça pode trocar de técnico."
                );
            }

            Tecnico responsavelAnterior = ordemServico.getTecnicoResponsavel();

            if (responsavelAnterior == null) {
                throw new DomainException(
                        "A OS não possui técnico responsável. "
                                + "Utilize a atribuição inicial."
                );
            }

            Tecnico novoTecnico = buscarTecnico(novoTecnicoId);

            if (responsavelAnterior.getId().equals(novoTecnico.getId())) {
                throw new DomainException(
                        "Este técnico já é o responsável pela OS."
                );
            }

            if (novoTecnico.getDisponibilidade() == DisponibilidadeTecnico.AUSENTE) {
                throw new DomainException(
                        "Técnico ausente não pode receber ordem de serviço."
                );
            }

            Optional<OrdemServico> ordemAtiva =
                    ordemServicoRepository.buscarAtivaPorTecnico(novoTecnicoId);

            if (ordemAtiva.isPresent()) {
                throw new DomainException(
                        "O técnico está atendendo a OS #"
                                + ordemAtiva.get().getId()
                                + ". A reatribuição não foi realizada."
                );
            }

            if (filaTecnicoRepository
                    .buscarPrimeiroPorTecnico(novoTecnicoId)
                    .isPresent()) {
                throw new DomainException(
                        "O técnico possui uma fila pendente. "
                                + "A reatribuição não pode ultrapassar essa fila."
                );
            }

            ordemServico.reatribuirTecnico(novoTecnico);
            OrdemServico persistida = ordemServicoRepository.salvar(ordemServico);

            novoTecnico.entrarEmAtendimento();
            tecnicoRepository.salvar(novoTecnico);

            encaminharProximaDaFila(buscarTecnico(responsavelAnterior.getId()));

            return persistida;
        });
    }

    public OrdemServico registrarDiagnostico(
            Long ordemServicoId,
            Long tecnicoId,
            String parecer,
            String causaRaiz) {

        Tecnico tecnico = buscarTecnico(tecnicoId);
        OrdemServico ordemServico = buscarPorId(ordemServicoId);

        Diagnostico diagnostico = new Diagnostico(
                parecer, causaRaiz, LocalDateTime.now(), tecnico
        );

        ordemServico.registrarDiagnostico(diagnostico);
        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico iniciarExecucao(Long ordemServicoId, Long tecnicoId) {
        Tecnico tecnico = buscarTecnico(tecnicoId);
        OrdemServico ordemServico = buscarPorId(ordemServicoId);

        ordemServico.iniciarExecucao(tecnico);
        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico registrarIntervencao(
            Long ordemServicoId,
            Long tecnicoId,
            String descricao,
            double horasTrabalhadas) {

        Tecnico tecnico = buscarTecnico(tecnicoId);
        OrdemServico ordemServico = buscarPorId(ordemServicoId);

        Intervencao intervencao = new Intervencao(
                null, LocalDateTime.now(), tecnico, descricao, horasTrabalhadas
        );

        ordemServico.registrarIntervencao(intervencao);
        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico adicionarMaterialIntervencao(
            Long ordemServicoId,
            Long tecnicoId,
            Long intervencaoId,
            String descricao,
            double quantidade,
            String unidade) {

        Tecnico tecnico = buscarTecnico(tecnicoId);
        OrdemServico ordemServico = buscarPorId(ordemServicoId);

        MaterialUtilizado material = new MaterialUtilizado(
                descricao, quantidade, unidade
        );

        ordemServico.adicionarMaterialIntervencao(
                tecnico, intervencaoId, material
        );

        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico aguardarPeca(Long ordemServicoId, Long tecnicoId) {
        Tecnico tecnico = buscarTecnico(tecnicoId);
        OrdemServico ordemServico = buscarPorId(ordemServicoId);

        ordemServico.aguardarPeca(tecnico);
        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico retomarExecucao(Long ordemServicoId, Long tecnicoId) {
        Tecnico tecnico = buscarTecnico(tecnicoId);
        OrdemServico ordemServico = buscarPorId(ordemServicoId);

        ordemServico.retomarExecucao(tecnico);
        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico finalizarReparo(Long ordemServicoId, Long tecnicoId) {
        return gerenciadorTransacao.executar(() -> {
            Tecnico tecnico = buscarTecnico(tecnicoId);
            OrdemServico ordemServico = buscarPorId(ordemServicoId);

            ordemServico.finalizarReparo(tecnico);
            OrdemServico persistida = ordemServicoRepository.salvar(ordemServico);

            encaminharProximaDaFila(tecnico);

            return persistida;
        });
    }

    public OrdemServico submeterParaAprovacao(
            Long ordemServicoId, Long tecnicoId) {

        Tecnico tecnico = buscarTecnico(tecnicoId);
        OrdemServico ordemServico = buscarPorId(ordemServicoId);

        ordemServico.submeterParaAprovacao(tecnico);
        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico encerrar(Long ordemServicoId, Long gestorId) {
        buscarUsuarioGestor(gestorId);
        OrdemServico ordemServico = buscarPorId(ordemServicoId);

        ordemServico.encerrar();
        return ordemServicoRepository.salvar(ordemServico);
    }

    public OrdemServico cancelar(Long ordemServicoId, Long gestorId) {
        return gerenciadorTransacao.executar(() -> {
            buscarUsuarioGestor(gestorId);

            OrdemServico ordemServico = buscarPorId(ordemServicoId);
            boolean ocupavaTecnico = ordemServico.getStatus().ocupaTecnico();
            Tecnico responsavel = ordemServico.getTecnicoResponsavel();

            Optional<ItemFilaTecnico> entradaNaFila =
                    filaTecnicoRepository.buscarPorOrdemServico(ordemServicoId);

            ordemServico.cancelar();
            OrdemServico persistida = ordemServicoRepository.salvar(ordemServico);

            if (entradaNaFila.isPresent()) {
                filaTecnicoRepository.removerPorOrdemServico(ordemServicoId);
            }

            if (ocupavaTecnico && responsavel != null) {
                encaminharProximaDaFila(buscarTecnico(responsavel.getId()));
            } else if (entradaNaFila.isPresent()) {
                encaminharProximaDaFila(
                        buscarTecnico(entradaNaFila.get().getTecnicoId())
                );
            }

            return persistida;
        });
    }

    // Deve ser chamado dentro de uma transação compartilhada.
    private void encaminharProximaDaFila(Tecnico tecnico) {
        if (tecnico.getDisponibilidade() == DisponibilidadeTecnico.AUSENTE) {
            return;
        }

        Long tecnicoId = tecnico.getId();

        Optional<OrdemServico> ordemAtiva =
                ordemServicoRepository.buscarAtivaPorTecnico(tecnicoId);

        if (ordemAtiva.isPresent()) {
            tecnico.entrarEmAtendimento();
            tecnicoRepository.salvar(tecnico);
            return;
        }

        Optional<ItemFilaTecnico> primeiraEntrada =
                filaTecnicoRepository.buscarPrimeiroPorTecnico(tecnicoId);

        if (primeiraEntrada.isEmpty()) {
            tecnico.ficarDisponivel();
            tecnicoRepository.salvar(tecnico);
            return;
        }

        ItemFilaTecnico entrada = primeiraEntrada.get();
        OrdemServico proximaOrdem = buscarPorId(entrada.getOrdemServicoId());

        if (proximaOrdem.getStatus() != StatusOrdemServico.ABERTA
                || proximaOrdem.getTecnicoResponsavel() != null) {
            throw new DomainException(
                    "Não foi possível encaminhar a fila: a OS #"
                            + proximaOrdem.getId()
                            + " precisa estar aberta e sem responsável."
            );
        }

        proximaOrdem.atribuirTecnico(tecnico);
        ordemServicoRepository.salvar(proximaOrdem);
        filaTecnicoRepository.removerPorOrdemServico(proximaOrdem.getId());

        tecnico.entrarEmAtendimento();
        tecnicoRepository.salvar(tecnico);
    }

    private Usuario buscarUsuarioGestor(Long usuarioId) {
        Usuario usuario = usuarioRepository
                .buscarPorId(usuarioId)
                .orElseThrow(() ->
                        new DomainException("Usuário não encontrado.")
                );

        if (usuario.getPerfil() != PerfilUsuario.GESTOR) {
            throw new DomainException("Esta operação exige perfil GESTOR.");
        }

        return usuario;
    }

    private Tecnico buscarTecnico(Long tecnicoId) {
        return tecnicoRepository
                .buscarPorId(tecnicoId)
                .orElseThrow(() ->
                        new DomainException("Técnico não encontrado.")
                );
    }
}