package br.com.manutencao.application.service;

import br.com.manutencao.domain.equipamento.Equipamento;
import br.com.manutencao.domain.equipamento.StatusEquipamento;
import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.ordemservico.Criticidade;
import br.com.manutencao.domain.ordemservico.Diagnostico;
import br.com.manutencao.domain.ordemservico.Intervencao;
import br.com.manutencao.domain.ordemservico.MaterialUtilizado;
import br.com.manutencao.domain.ordemservico.OrdemServico;
import br.com.manutencao.domain.ordemservico.StatusOrdemServico;
import br.com.manutencao.domain.repository.EquipamentoRepository;
import br.com.manutencao.domain.repository.OrdemServicoRepository;
import br.com.manutencao.domain.repository.TecnicoRepository;
import br.com.manutencao.domain.repository.UsuarioRepository;
import br.com.manutencao.domain.usuario.DisponibilidadeTecnico;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Tecnico;
import br.com.manutencao.domain.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;

    public OrdemServicoService(
            OrdemServicoRepository ordemServicoRepository,
            EquipamentoRepository equipamentoRepository,
            UsuarioRepository usuarioRepository,
            TecnicoRepository tecnicoRepository) {

        if (ordemServicoRepository == null) {
            throw new IllegalArgumentException(
                    "OrdemServicoRepository é obrigatório."
            );
        }

        if (equipamentoRepository == null) {
            throw new IllegalArgumentException(
                    "EquipamentoRepository é obrigatório."
            );
        }

        if (usuarioRepository == null) {
            throw new IllegalArgumentException(
                    "UsuarioRepository é obrigatório."
            );
        }

        if (tecnicoRepository == null) {
            throw new IllegalArgumentException(
                    "TecnicoRepository é obrigatório."
            );
        }

        this.ordemServicoRepository =
                ordemServicoRepository;

        this.equipamentoRepository =
                equipamentoRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.tecnicoRepository =
                tecnicoRepository;
    }

    public OrdemServico abrirOrdemServico(
            Long equipamentoId,
            Long usuarioAberturaId,
            String descricaoProblema,
            Criticidade criticidade) {

        Equipamento equipamento =
                equipamentoRepository
                        .buscarPorId(equipamentoId)
                        .orElseThrow(() ->
                                new DomainException(
                                        "Equipamento não encontrado."
                                )
                        );

        if (equipamento.getStatus()
                != StatusEquipamento.ATIVO) {

            throw new DomainException(
                    "Não é possível abrir uma ordem de serviço "
                    + "para equipamento inativo."
            );
        }

        Usuario usuario =
                usuarioRepository
                        .buscarPorId(usuarioAberturaId)
                        .orElseThrow(() ->
                                new DomainException(
                                        "Usuário não encontrado."
                                )
                        );

        OrdemServico ordemServico =
                new OrdemServico(
                        null,
                        equipamento,
                        usuario,
                        descricaoProblema,
                        criticidade
                );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico buscarPorId(Long id) {

        return ordemServicoRepository
                .buscarPorId(id)
                .orElseThrow(() ->
                        new DomainException(
                                "Ordem de serviço não encontrada."
                        )
                );
    }

    public List<OrdemServico> listarTodos() {

        return ordemServicoRepository.listarTodos();
    }

    public List<OrdemServico> listarPorStatus(
            StatusOrdemServico status) {

        return ordemServicoRepository
                .listarPorStatus(status);
    }

    public OrdemServico atribuirTecnico(
            Long ordemServicoId,
            Long gestorId,
            Long tecnicoId) {

        buscarUsuarioGestor(gestorId);

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        if (tecnico.getDisponibilidade()
                == DisponibilidadeTecnico.AUSENTE) {

            throw new DomainException(
                    "Técnico ausente não pode receber ordem de serviço."
            );
        }

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.atribuirTecnico(
                tecnico
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico reatribuirTecnico(
            Long ordemServicoId,
            Long gestorId,
            Long novoTecnicoId) {

        buscarUsuarioGestor(gestorId);

        Tecnico novoTecnico =
                buscarTecnico(novoTecnicoId);

        if (novoTecnico.getDisponibilidade()
                == DisponibilidadeTecnico.AUSENTE) {

            throw new DomainException(
                    "Técnico ausente não pode receber ordem de serviço."
            );
        }

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.reatribuirTecnico(
                novoTecnico
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico registrarDiagnostico(
            Long ordemServicoId,
            Long tecnicoId,
            String parecer,
            String causaRaiz) {

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        Diagnostico diagnostico =
                new Diagnostico(
                        parecer,
                        causaRaiz,
                        LocalDateTime.now(),
                        tecnico
                );

        ordemServico.registrarDiagnostico(
                diagnostico
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico iniciarExecucao(
            Long ordemServicoId,
            Long tecnicoId) {

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.iniciarExecucao(
                tecnico
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico registrarIntervencao(
            Long ordemServicoId,
            Long tecnicoId,
            String descricao,
            double horasTrabalhadas) {

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        Intervencao intervencao =
                new Intervencao(
                        null,
                        LocalDateTime.now(),
                        tecnico,
                        descricao,
                        horasTrabalhadas
                );

        ordemServico.registrarIntervencao(
                intervencao
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico adicionarMaterialIntervencao(
            Long ordemServicoId,
            Long tecnicoId,
            Long intervencaoId,
            String descricao,
            double quantidade,
            String unidade) {

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        MaterialUtilizado material =
                new MaterialUtilizado(
                        descricao,
                        quantidade,
                        unidade
                );

        ordemServico.adicionarMaterialIntervencao(
                tecnico,
                intervencaoId,
                material
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico aguardarPeca(
            Long ordemServicoId,
            Long tecnicoId) {

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.aguardarPeca(
                tecnico
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico retomarExecucao(
            Long ordemServicoId,
            Long tecnicoId) {

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.retomarExecucao(
                tecnico
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico finalizarReparo(
            Long ordemServicoId,
            Long tecnicoId) {

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.finalizarReparo(
                tecnico
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico submeterParaAprovacao(
            Long ordemServicoId,
            Long tecnicoId) {

        Tecnico tecnico =
                buscarTecnico(tecnicoId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.submeterParaAprovacao(
                tecnico
        );

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico encerrar(
            Long ordemServicoId,
            Long gestorId) {

        buscarUsuarioGestor(gestorId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.encerrar();

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    public OrdemServico cancelar(
            Long ordemServicoId,
            Long gestorId) {

        buscarUsuarioGestor(gestorId);

        OrdemServico ordemServico =
                buscarPorId(ordemServicoId);

        ordemServico.cancelar();

        return ordemServicoRepository.salvar(
                ordemServico
        );
    }

    private Usuario buscarUsuarioGestor(
            Long usuarioId) {

        Usuario usuario =
                usuarioRepository
                        .buscarPorId(usuarioId)
                        .orElseThrow(() ->
                                new DomainException(
                                        "Usuário não encontrado."
                                )
                        );

        if (usuario.getPerfil()
                != PerfilUsuario.GESTOR) {

            throw new DomainException(
                    "Esta operação exige perfil GESTOR."
            );
        }

        return usuario;
    }

    private Tecnico buscarTecnico(
            Long tecnicoId) {

        return tecnicoRepository
                .buscarPorId(tecnicoId)
                .orElseThrow(() ->
                        new DomainException(
                                "Técnico não encontrado."
                        )
                );
    }
}