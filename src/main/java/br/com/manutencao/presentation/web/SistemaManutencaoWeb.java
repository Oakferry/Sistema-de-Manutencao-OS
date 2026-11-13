package br.com.manutencao.presentation.web;

import br.com.manutencao.application.dto.ResultadoAtribuicao;
import br.com.manutencao.application.service.EquipamentoService;
import br.com.manutencao.application.service.OrdemServicoService;
import br.com.manutencao.application.service.TecnicoService;
import br.com.manutencao.application.service.UsuarioService;
import br.com.manutencao.domain.equipamento.StatusEquipamento;
import br.com.manutencao.domain.exception.DomainException;
import br.com.manutencao.domain.ordemservico.Criticidade;
import br.com.manutencao.domain.ordemservico.OrdemServico;
import br.com.manutencao.domain.usuario.DisponibilidadeTecnico;
import br.com.manutencao.domain.usuario.PerfilUsuario;
import br.com.manutencao.domain.usuario.Tecnico;
import br.com.manutencao.infrastructure.persistence.sqlite.DatabaseInitializer;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteEquipamentoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteFilaTecnicoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteGerenciadorTransacao;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteOrdemServicoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteTecnicoRepository;
import br.com.manutencao.infrastructure.persistence.sqlite.SqliteUsuarioRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SistemaManutencaoWeb {

    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 8080;
    private static final int LIMITE_CORPO = 16_384;

    private final SqliteEquipamentoRepository equipamentos = new SqliteEquipamentoRepository();
    private final SqliteUsuarioRepository usuarios = new SqliteUsuarioRepository();
    private final SqliteTecnicoRepository tecnicos = new SqliteTecnicoRepository();
    private final SqliteOrdemServicoRepository repositorioOrdens = new SqliteOrdemServicoRepository();
    private final SqliteFilaTecnicoRepository fila = new SqliteFilaTecnicoRepository();
    private final SqliteGerenciadorTransacao transacao = new SqliteGerenciadorTransacao();

    private final EquipamentoService cadastroEquipamentos =
            new EquipamentoService(equipamentos, repositorioOrdens);
    private final UsuarioService cadastroUsuarios = new UsuarioService(usuarios);
    private final TecnicoService cadastroTecnicos = new TecnicoService(tecnicos, usuarios);
    private final OrdemServicoService ordens = new OrdemServicoService(
            repositorioOrdens, equipamentos, usuarios, tecnicos, fila, transacao
    );

    public static void main(String[] args) throws Exception {
        DatabaseInitializer.initialize();
        new SistemaManutencaoWeb().iniciar();
    }

    private void iniciar() throws IOException {
        byte[] pagina = recurso("/web/index.html");
        byte[] css = recurso("/web/styles.css");
        byte[] js = recurso("/web/app.js");
        byte[] jsCadastros = recurso("/web/cadastros.js");

        HttpServer servidor = HttpServer.create(new InetSocketAddress(HOST, PORTA), 0);
        servidor.createContext("/", ex -> atender(ex, () -> {
            String caminho = ex.getRequestURI().getPath();
            String metodo = ex.getRequestMethod();

            switch (caminho) {
                case "/" -> enviarArquivo(ex, metodo, "text/html", pagina);
                case "/styles.css" -> enviarArquivo(ex, metodo, "text/css", css);
                case "/app.js" -> enviarArquivo(ex, metodo, "text/javascript", js);
                case "/cadastros.js" -> enviarArquivo(ex, metodo, "text/javascript", jsCadastros);
                case "/api/painel" -> {
                    if (permitir(ex, metodo, "GET", "GET")) {
                        enviarJson(ex, 200, painel());
                    }
                }
                case "/api/ordens" -> {
                    if ("GET".equals(metodo)) {
                        enviarJson(ex, 200, ordens.listarTodos().stream()
                                .map(SistemaManutencaoWeb::dadosOrdem).toList());
                    } else if (permitir(ex, metodo, "POST", "GET, POST")) {
                        abrir(ex, lerFormulario(ex));
                    }
                }
                case "/api/ordens/acao" -> {
                    if (permitir(ex, metodo, "POST", "POST")) {
                        executarAcao(ex, lerFormulario(ex));
                    }
                }
                case "/api/cadastros" -> {
                    if (permitir(ex, metodo, "POST", "POST")) {
                        Map<String, String> campos = lerFormulario(ex);
                        String mensagem = transacao.executar(() -> executarCadastro(campos));
                        enviarJson(ex, 200, objeto("mensagem", mensagem));
                    }
                }
                default -> enviarJson(ex, 404, objeto("erro", "Rota não encontrada."));
            }
        }));

        servidor.start();
        System.out.println("Servidor iniciado em http://" + HOST + ":" + PORTA + "/");
    }

    private Map<String, Object> painel() {
        return objeto(
                "equipamentos", cadastroEquipamentos.listarTodos().stream().map(e -> objeto(
                        "id", e.getId(), "nome", e.getNome(), "tipo", e.getTipo(),
                        "identificador", e.getIdentificador(), "descricao", e.getDescricao(),
                        "status", e.getStatus().name(),
                        "ativo", e.getStatus() == StatusEquipamento.ATIVO
                )).toList(),
                "usuarios", cadastroUsuarios.listarTodos().stream().map(u -> objeto(
                        "id", u.getId(), "nome", u.getNome(), "login", u.getLogin(),
                        "perfil", u.getPerfil().name()
                )).toList(),
                "tecnicos", cadastroTecnicos.listarTodos().stream().map(t -> objeto(
                        "id", t.getId(), "usuarioId", t.getUsuario().getId(),
                        "nome", t.getUsuario().getNome(),
                        "disponibilidade", t.getDisponibilidade().name(),
                        "fila", fila.listarPorTecnico(t.getId()).stream()
                                .map(i -> i.getOrdemServicoId().toString()).toList()
                )).toList(),
                "criticidades", Arrays.stream(Criticidade.values()).map(Enum::name).toList(),
                "perfis", Arrays.stream(PerfilUsuario.values()).map(Enum::name).toList(),
                "disponibilidades", Arrays.stream(DisponibilidadeTecnico.values())
                        .map(Enum::name).toList(),
                "ordens", ordens.listarTodos().stream()
                        .map(SistemaManutencaoWeb::dadosOrdem).toList()
        );
    }

    private String executarCadastro(Map<String, String> c) {
        return switch (texto(c, "acao", 40)) {
            case "criar-equipamento" -> {
                cadastroEquipamentos.cadastrar(
                        texto(c, "nome", 120), texto(c, "descricao", 500),
                        texto(c, "tipo", 80), texto(c, "identificador", 80));
                yield "Equipamento cadastrado.";
            }
            case "editar-equipamento" -> {
                cadastroEquipamentos.editar(
                        id(c, "id"), texto(c, "nome", 120), texto(c, "descricao", 500),
                        texto(c, "tipo", 80), texto(c, "identificador", 80));
                yield "Equipamento atualizado.";
            }
            case "ativar-equipamento" -> {
                cadastroEquipamentos.ativar(id(c, "id"));
                yield "Equipamento ativado.";
            }
            case "inativar-equipamento" -> {
                cadastroEquipamentos.inativar(id(c, "id"));
                yield "Equipamento inativado.";
            }
            case "excluir-equipamento" -> {
                cadastroEquipamentos.excluir(id(c, "id"));
                yield "Equipamento excluído.";
            }
            case "criar-usuario" -> {
                cadastroUsuarios.criarUsuario(
                        texto(c, "nome", 120), texto(c, "login", 80), perfil(c));
                yield "Usuário cadastrado.";
            }
            case "editar-usuario" -> {
                cadastroUsuarios.editarUsuario(
                        id(c, "id"), texto(c, "nome", 120), texto(c, "login", 80));
                yield "Usuário atualizado.";
            }
            case "alterar-perfil" -> {
                long usuarioId = id(c, "id");
                PerfilUsuario novoPerfil = perfil(c);
                if (novoPerfil != PerfilUsuario.TECNICO
                        && tecnicos.buscarPorUsuarioId(usuarioId).isPresent()) {
                    throw new DomainException(
                            "Este usuário possui cadastro técnico e deve manter o perfil Técnico.");
                }
                cadastroUsuarios.definirPerfil(usuarioId, novoPerfil);
                yield "Perfil atualizado.";
            }
            case "criar-tecnico" -> {
                cadastroTecnicos.criarTecnico(id(c, "usuarioId"));
                yield "Usuário vinculado ao cadastro técnico.";
            }
            case "alterar-disponibilidade" -> {
                alterarDisponibilidade(id(c, "id"), disponibilidade(c));
                yield "Disponibilidade atualizada.";
            }
            default -> throw new IllegalArgumentException("Operação de cadastro inválida.");
        };
    }

    private void alterarDisponibilidade(long tecnicoId, DisponibilidadeTecnico nova) {
        Tecnico tecnico = cadastroTecnicos.buscarPorId(tecnicoId);
        if (tecnico.getDisponibilidade() == nova) return;

        var ordemAtiva = repositorioOrdens.buscarAtivaPorTecnico(tecnicoId);
        if (ordemAtiva.isPresent() && nova != DisponibilidadeTecnico.EM_ATENDIMENTO) {
            throw new DomainException("O técnico ainda está atendendo a OS #"
                    + ordemAtiva.get().getId()
                    + ". Finalize, cancele ou reatribua essa ordem antes de mudar a disponibilidade.");
        }

        if (ordemAtiva.isEmpty()
                && nova == DisponibilidadeTecnico.DISPONIVEL
                && fila.buscarPrimeiroPorTecnico(tecnicoId).isPresent()) {
            throw new DomainException(
                    "O técnico tem ordens pendentes na fila. Resolva a fila antes de marcá-lo disponível.");
        }

        switch (nova) {
            case DISPONIVEL -> cadastroTecnicos.ficarDisponivel(tecnicoId);
            case EM_ATENDIMENTO -> cadastroTecnicos.entrarEmAtendimento(tecnicoId);
            case AUSENTE -> cadastroTecnicos.ficarAusente(tecnicoId);
        }
    }

    private static PerfilUsuario perfil(Map<String, String> c) {
        try {
            return PerfilUsuario.valueOf(texto(c, "perfil", 30));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Selecione um perfil válido.");
        }
    }

    private static DisponibilidadeTecnico disponibilidade(Map<String, String> c) {
        try {
            return DisponibilidadeTecnico.valueOf(texto(c, "disponibilidade", 30));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Selecione uma disponibilidade válida.");
        }
    }

    private static Map<String, Object> dadosOrdem(OrdemServico os) {
        Tecnico tecnico = os.getTecnicoResponsavel();
        return objeto(
                "id", os.getId(), "descricao", os.getDescricaoProblema(),
                "status", os.getStatus().name(), "criticidade", os.getCriticidade().name(),
                "equipamento", os.getEquipamento().getNome(),
                "solicitante", os.getUsuarioAbertura().getNome(),
                "ocupaTecnico", os.getStatus().ocupaTecnico(),
                "tecnicoId", tecnico == null ? null : tecnico.getId().toString(),
                "diagnostico", os.getDiagnostico() == null ? null : objeto(
                        "parecer", os.getDiagnostico().getParecer(),
                        "causa", os.getDiagnostico().getCausaRaiz()),
                "intervencoes", os.getIntervencoes().stream().map(i -> objeto(
                        "id", i.getId(), "descricao", i.getDescricao(),
                        "tecnicoId", i.getTecnico().getId().toString(),
                        "horas", i.getHorasTrabalhadas(),
                        "materiais", i.getMateriais().stream().map(m -> objeto(
                                "descricao", m.getDescricao(), "quantidade", m.getQuantidade(),
                                "unidade", m.getUnidade())).toList()
                )).toList()
        );
    }

    private void abrir(HttpExchange ex, Map<String, String> c) throws IOException {
        Criticidade criticidade;
        try {
            criticidade = Criticidade.valueOf(texto(c, "criticidade", 30));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Selecione uma criticidade válida.");
        }

        OrdemServico os = ordens.abrirOrdemServico(
                id(c, "equipamentoId"), id(c, "usuarioId"),
                texto(c, "descricao", 500), criticidade
        );
        enviarJson(ex, 201, objeto(
                "id", os.getId(), "mensagem", "OS #" + os.getId() + " aberta com sucesso."));
    }

    private void executarAcao(HttpExchange ex, Map<String, String> c) throws IOException {
        long osId = id(c, "ordemId");
        String mensagem = "OS #" + osId + " atualizada.";

        switch (texto(c, "acao", 40)) {
            case "atribuir" -> {
                ResultadoAtribuicao resultado = ordens.atribuirTecnico(
                        osId, id(c, "gestorId"), id(c, "tecnicoId"));
                mensagem = resultado.enfileirada()
                        ? "OS #" + osId + " entrou na fila na posição "
                                + resultado.posicaoFila() + "."
                        : "OS #" + osId + " atribuída.";
            }
            case "reatribuir" -> ordens.reatribuirTecnico(
                    osId, id(c, "gestorId"), id(c, "tecnicoId"));
            case "diagnosticar" -> ordens.registrarDiagnostico(
                    osId, id(c, "tecnicoId"), texto(c, "parecer", 500), texto(c, "causa", 500));
            case "iniciar" -> ordens.iniciarExecucao(osId, id(c, "tecnicoId"));
            case "intervir" -> ordens.registrarIntervencao(
                    osId, id(c, "tecnicoId"), texto(c, "descricao", 500), positivo(c, "horas"));
            case "material" -> ordens.adicionarMaterialIntervencao(
                    osId, id(c, "tecnicoId"), id(c, "intervencaoId"),
                    texto(c, "descricao", 500), positivo(c, "quantidade"), texto(c, "unidade", 30));
            case "aguardar" -> ordens.aguardarPeca(osId, id(c, "tecnicoId"));
            case "retomar" -> ordens.retomarExecucao(osId, id(c, "tecnicoId"));
            case "finalizar" -> ordens.finalizarReparo(osId, id(c, "tecnicoId"));
            case "submeter" -> ordens.submeterParaAprovacao(osId, id(c, "tecnicoId"));
            case "encerrar" -> ordens.encerrar(osId, id(c, "gestorId"));
            case "cancelar" -> ordens.cancelar(osId, id(c, "gestorId"));
            default -> throw new IllegalArgumentException("Ação inválida.");
        }

        enviarJson(ex, 200, objeto("mensagem", mensagem));
    }

    private static Map<String, String> lerFormulario(HttpExchange ex) throws IOException {
        String tipo = ex.getRequestHeaders().getFirst("Content-Type");
        if (tipo == null || !tipo.split(";", 2)[0].strip()
                .equalsIgnoreCase("application/x-www-form-urlencoded")
                || !"1".equals(ex.getRequestHeaders().getFirst("X-Formulario-Manutencao"))) {
            throw new IllegalArgumentException("Envie a ação pelo formulário de manutenção.");
        }

        byte[] bytes = ex.getRequestBody().readNBytes(LIMITE_CORPO + 1);
        if (bytes.length > LIMITE_CORPO) {
            throw new IllegalArgumentException("Formulário grande demais.");
        }

        Map<String, String> campos = new LinkedHashMap<>();
        for (String par : new String(bytes, StandardCharsets.UTF_8).split("&")) {
            String[] partes = par.split("=", 2);
            if (partes.length == 2) {
                campos.put(URLDecoder.decode(partes[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(partes[1], StandardCharsets.UTF_8));
            }
        }
        return campos;
    }

    private static String texto(Map<String, String> c, String campo, int max) {
        String valor = c.getOrDefault(campo, "").strip();
        if (valor.isEmpty() || valor.length() > max) {
            throw new IllegalArgumentException("Confira o campo " + campo + ".");
        }
        return valor;
    }

    private static long id(Map<String, String> c, String campo) {
        try {
            long valor = Long.parseLong(c.getOrDefault(campo, ""));
            if (valor > 0) return valor;
        } catch (NumberFormatException ignorado) {
        }
        throw new IllegalArgumentException("Selecione uma opção válida para " + campo + ".");
    }

    private static double positivo(Map<String, String> c, String campo) {
        try {
            double valor = Double.parseDouble(c.getOrDefault(campo, ""));
            if (Double.isFinite(valor) && valor > 0) return valor;
        } catch (NumberFormatException ignorado) {
        }
        throw new IllegalArgumentException("Informe um valor positivo para " + campo + ".");
    }

    private static byte[] recurso(String nome) throws IOException {
        try (InputStream in = SistemaManutencaoWeb.class.getResourceAsStream(nome)) {
            if (in == null) throw new IOException("Arquivo não encontrado: " + nome);
            return in.readAllBytes();
        }
    }

    private static boolean permitir(HttpExchange ex, String atual, String esperado,
                                    String permitidos) throws IOException {
        if (esperado.equals(atual)) return true;
        ex.getResponseHeaders().set("Allow", permitidos);
        enviarJson(ex, 405, objeto("erro", "Método não permitido."));
        return false;
    }

    private static void enviarArquivo(HttpExchange ex, String metodo, String tipo, byte[] bytes)
            throws IOException {
        if (permitir(ex, metodo, "GET", "GET")) enviar(ex, 200, tipo, bytes);
    }

    @FunctionalInterface
    private interface Requisicao {
        void executar() throws IOException;
    }

    private static void atender(HttpExchange ex, Requisicao requisicao) throws IOException {
        try (ex) {
            try {
                requisicao.executar();
            } catch (DomainException e) {
                enviarJson(ex, 422, objeto("erro", e.getMessage()));
            } catch (IllegalArgumentException e) {
                enviarJson(ex, 400, objeto("erro", e.getMessage()));
            } catch (RuntimeException e) {
                e.printStackTrace();
                enviarJson(ex, 500, objeto("erro", "Não foi possível concluir a operação."));
            }
        }
    }

    private static Map<String, Object> objeto(Object... pares) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            mapa.put((String) pares[i], pares[i + 1]);
        }
        return mapa;
    }

    private static String json(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof Boolean) return valor.toString();
        if (valor instanceof Number numero) {
            if (numero instanceof Long) return citar(numero.toString());
            return Double.isFinite(numero.doubleValue()) ? numero.toString() : "null";
        }
        if (valor instanceof Map<?, ?> mapa) {
            return mapa.entrySet().stream()
                    .map(e -> citar(e.getKey().toString()) + ":" + json(e.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
        if (valor instanceof Iterable<?> lista) {
            return StreamSupport.stream(lista.spliterator(), false)
                    .map(SistemaManutencaoWeb::json)
                    .collect(Collectors.joining(",", "[", "]"));
        }
        return citar(valor.toString());
    }

    private static String citar(String texto) {
        StringBuilder saida = new StringBuilder("\"");
        for (char c : texto.toCharArray()) {
            if (c == '"' || c == '\\') saida.append('\\').append(c);
            else if (c < 32) saida.append(String.format("\\u%04x", (int) c));
            else saida.append(c);
        }
        return saida.append('"').toString();
    }

    private static void enviarJson(HttpExchange ex, int codigo, Object corpo)
            throws IOException {
        enviar(ex, codigo, "application/json", json(corpo).getBytes(StandardCharsets.UTF_8));
    }

    private static void enviar(HttpExchange ex, int codigo, String tipo, byte[] corpo)
            throws IOException {
        ex.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(codigo, corpo.length);
        try (var out = ex.getResponseBody()) {
            out.write(corpo);
        }
    }
}