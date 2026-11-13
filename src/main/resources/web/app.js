"use strict";

const $ = seletor => document.querySelector(seletor);
const formulario = $("#abrir");
const dialogo = $("#dialogo");
const formAcao = $("#form-acao");
const retorno = $("#retorno");

const titulos = {
    ordens: "Ordens de serviço",
    fila: "Fila dos técnicos",
    cadastros: "Equipamentos e pessoas"
};

const rotulos = {
    ABERTA: "Aberta", ATRIBUIDA: "Atribuída", EM_EXECUCAO: "Em execução",
    AGUARDANDO_PECA: "Aguardando peça", REPARO_FINALIZADO: "Reparo finalizado",
    AGUARDANDO_APROVACAO: "Aguardando aprovação", ENCERRADA: "Encerrada",
    CANCELADA: "Cancelada", DISPONIVEL: "Disponível", AUSENTE: "Ausente",
    EM_ATENDIMENTO: "Em atendimento", ATIVO: "Ativo", INATIVO: "Inativo",
    GESTOR: "Gestor", TECNICO: "Técnico", BAIXA: "Baixa", MEDIA: "Média",
    ALTA: "Alta", CRITICA: "Crítica"
};

let dados = null;
let ocupado = false;
let acaoAtual = null;

function rotulo(valor) {
    return rotulos[valor] ?? valor;
}

function criar(tag, texto = "", classe = "") {
    const item = document.createElement(tag);
    item.textContent = texto;
    if (classe) item.className = classe;
    return item;
}

function mostrar(texto, erro = false) {
    retorno.textContent = texto;
    retorno.classList.toggle("erro", erro);
}

function navegar() {
    const hash = location.hash.slice(1);
    const tela = Object.hasOwn(titulos, hash) ? hash : "ordens";
    $("#titulo").textContent = titulos[tela];

    document.querySelectorAll(".tela").forEach(secao => {
        secao.hidden = secao.id !== tela;
    });
    document.querySelectorAll("nav a").forEach(link => {
        if (link.hash === "#" + tela) link.setAttribute("aria-current", "page");
        else link.removeAttribute("aria-current");
    });
}

async function api(url, campos) {
    const resposta = await fetch(url, campos ? {
        method: "POST",
        headers: { "X-Formulario-Manutencao": "1" },
        body: new URLSearchParams(campos)
    } : { cache: "no-store" });

    const corpo = await resposta.json();
    if (!resposta.ok) throw new Error(corpo.erro ?? "A operação falhou.");
    return corpo;
}

function opcoes(select, itens, texto) {
    const anterior = select.value;
    select.replaceChildren(new Option(itens.length ? "Selecione" : "Sem opções", ""));
    for (const item of itens) select.add(new Option(texto(item), item.id));
    if (itens.some(item => String(item.id) === anterior)) select.value = anterior;
    select.disabled = itens.length === 0;
}

function habilitarAbertura() {
    formulario.querySelector("button").disabled =
        ocupado || !dados || [...formulario.querySelectorAll("select")].some(s => s.disabled);
}

function preencherAbertura() {
    opcoes(formulario.elements.equipamentoId, dados.equipamentos.filter(e => e.ativo),
        e => `${e.nome} · ${e.identificador}`);
    opcoes(formulario.elements.usuarioId, dados.usuarios,
        u => `${u.nome} · ${rotulo(u.perfil)}`);
    opcoes(formulario.elements.criticidade, dados.criticidades.map(id => ({ id })),
        c => rotulo(c.id));
    habilitarAbertura();
}

function tecnicoPorId(id) {
    return dados.tecnicos.find(t => String(t.id) === String(id));
}

function filaDaOs(id) {
    for (const tecnico of dados.tecnicos) {
        const posicao = tecnico.fila.indexOf(String(id));
        if (posicao >= 0) return { tecnico, posicao: posicao + 1 };
    }
    return null;
}

function acoesDaOs(os) {
    const acoes = [];
    const naFila = filaDaOs(os.id);
    if (os.status === "ABERTA" && !naFila) acoes.push(["atribuir", "Atribuir"]);
    if (os.ocupaTecnico && os.tecnicoId) acoes.push(["reatribuir", "Reatribuir"]);

    if (os.tecnicoId) {
        if (os.status === "ATRIBUIDA") {
            acoes.push(["diagnosticar", "Registrar diagnóstico"]);
            if (os.diagnostico) acoes.push(["iniciar", "Iniciar execução"]);
        }
        if (os.status === "EM_EXECUCAO") {
            acoes.push(["intervir", "Registrar intervenção"], ["aguardar", "Aguardar peça"]);
            if (os.intervencoes.some(i => i.tecnicoId === os.tecnicoId)) {
                acoes.push(["material", "Adicionar material"]);
            }
            if (os.intervencoes.length) acoes.push(["finalizar", "Finalizar reparo"]);
        }
        if (os.status === "AGUARDANDO_PECA") acoes.push(["retomar", "Retomar execução"]);
        if (os.status === "REPARO_FINALIZADO") acoes.push(["submeter", "Enviar para aprovação"]);
    }

    if (os.status === "AGUARDANDO_APROVACAO") acoes.push(["encerrar", "Aprovar e encerrar"]);
    if (!["ENCERRADA", "CANCELADA"].includes(os.status)) acoes.push(["cancelar", "Cancelar OS"]);

    return acoes;
}

function renderizarOrdens() {
    const lista = $("#lista-ordens");
    lista.replaceChildren();
    if (!dados.ordens.length) lista.append(criar("p", "Nenhuma ordem cadastrada.", "painel"));

    for (const os of dados.ordens) {
        const card = criar("article", "", "painel");
        const cabecalho = criar("div", "", "linha");
        const selo = criar("span", rotulo(os.status), "selo");
        selo.dataset.status = os.status;
        cabecalho.append(criar("h2", `OS #${os.id} · ${os.equipamento}`), selo);
        card.append(cabecalho, criar("p", os.descricao));

        const responsavel = tecnicoPorId(os.tecnicoId);
        const fila = filaDaOs(os.id);
        let atendimento = responsavel ? `Responsável: ${responsavel.nome}` : "Sem responsável";
        if (fila) atendimento = `Fila de ${fila.tecnico.nome} · posição ${fila.posicao}`;

        card.append(criar("p",
            `${rotulo(os.criticidade)} · Solicitante: ${os.solicitante} · ${atendimento}`, "meta"));

        if (os.diagnostico || os.intervencoes.length) {
            const historico = criar("details");
            historico.append(criar("summary", "Diagnóstico e intervenções"));
            if (os.diagnostico) {
                historico.append(criar("p",
                    `${os.diagnostico.parecer} · Causa: ${os.diagnostico.causa}`));
            }
            const itens = criar("ul");
            for (const i of os.intervencoes) {
                const li = criar("li", `${i.descricao} · ${i.horas} h`);
                for (const m of i.materiais) {
                    li.append(criar("div", `${m.descricao} · ${m.quantidade} ${m.unidade}`, "meta"));
                }
                itens.append(li);
            }
            if (itens.childElementCount) historico.append(itens);
            card.append(historico);
        }

        const botoes = criar("div", "", "botoes");
        for (const [acao, texto] of acoesDaOs(os)) {
            const botao = criar("button", texto, "secundario");
            botao.type = "button";
            botao.addEventListener("click", () => abrirAcao(os, acao, texto));
            botoes.append(botao);
        }
        if (botoes.childElementCount) card.append(botoes);
        lista.append(card);
    }
}

function renderizarFila() {
    const lista = $("#lista-fila");
    lista.replaceChildren();
    if (!dados.tecnicos.length) lista.append(criar("p", "Nenhum técnico cadastrado.", "painel"));

    for (const tecnico of dados.tecnicos) {
        const card = criar("article", "", "painel");
        const ativas = dados.ordens.filter(os =>
            os.ocupaTecnico && String(os.tecnicoId) === String(tecnico.id));

        card.append(criar("h2", tecnico.nome),
            criar("p", rotulo(tecnico.disponibilidade), "meta"),
            criar("p", ativas.length
                ? "Atendimento atual: " + ativas.map(os => `OS #${os.id} — ${os.descricao}`).join("; ")
                : "Sem ordem em atendimento."));

        if (tecnico.fila.length) {
            const fila = criar("ol");
            for (const id of tecnico.fila) {
                const os = dados.ordens.find(o => String(o.id) === String(id));
                fila.append(criar("li", `OS #${id}${os ? " — " + os.descricao : ""}`));
            }
            card.append(criar("h3", "Fila de espera"), fila);
        } else {
            card.append(criar("p", "Fila vazia.", "meta"));
        }
        lista.append(card);
    }
}

function renderizarCadastros() {
    CadastrosGerais.renderizar();
}
function seletor(pai, nome, titulo, itens, formatar) {
    const label = criar("label", titulo);
    const select = criar("select");
    select.name = nome;
    select.required = true;
    opcoes(select, itens, formatar);
    if (itens.length === 1) select.value = itens[0].id;
    label.append(select);
    pai.append(label);
    return itens.length > 0;
}

function campo(pai, nome, titulo, tipo = "text", limite = 500) {
    const label = criar("label", titulo);
    const input = criar("input");
    input.name = nome;
    input.type = tipo;
    input.required = true;
    if (tipo === "number") {
        input.min = "0.01";
        input.step = "any";
    } else input.maxLength = limite;
    label.append(input);
    pai.append(label);
}

function abrirAcao(os, acao, titulo) {
    if (ocupado) return;
    acaoAtual = { ordemId: os.id, acao };
    $("#titulo-acao").textContent = `${titulo} · OS #${os.id}`;
    $("#erro-acao").textContent = "";
    const campos = $("#campos-acao");
    campos.replaceChildren();
    let disponivel = true;

    if (["atribuir", "reatribuir", "cancelar", "encerrar"].includes(acao)) {
        disponivel = seletor(campos, "gestorId", "Gestor",
            dados.usuarios.filter(u => u.perfil === "GESTOR"), u => u.nome);
    }

    if (["atribuir", "reatribuir"].includes(acao)) {
        const tecnicos = dados.tecnicos.filter(t => {
            if (t.disponibilidade === "AUSENTE") return false;
            const comOrdem = dados.ordens.some(o =>
                o.ocupaTecnico && String(o.tecnicoId) === String(t.id));
            return acao === "atribuir" || (
                String(t.id) !== String(os.tecnicoId) && !comOrdem && !t.fila.length
            );
        });
        disponivel = seletor(campos, "tecnicoId", "Técnico", tecnicos,
            t => `${t.nome} · ${rotulo(t.disponibilidade)}`) && disponivel;
    } else if (!["cancelar", "encerrar"].includes(acao)) {
        acaoAtual.tecnicoId = os.tecnicoId;
        const tecnico = tecnicoPorId(os.tecnicoId);
        if (!tecnico) disponivel = false;
        else campos.append(criar("p", `Responsável: ${tecnico.nome}`, "meta"));
    }

    if (acao === "diagnosticar") {
        campo(campos, "parecer", "Parecer técnico");
        campo(campos, "causa", "Causa identificada");
    }
    if (acao === "intervir") {
        campo(campos, "descricao", "Serviço executado");
        campo(campos, "horas", "Horas trabalhadas", "number");
    }
    if (acao === "material") {
        disponivel = seletor(campos, "intervencaoId", "Intervenção",
            os.intervencoes.filter(i => i.tecnicoId === os.tecnicoId),
            i => i.descricao) && disponivel;
        campo(campos, "descricao", "Material utilizado");
        campo(campos, "quantidade", "Quantidade", "number");
        campo(campos, "unidade", "Unidade (ex.: un)", "text", 30);
    }

    if (acao === "cancelar") campos.append(criar("p", "Confirme o cancelamento desta ordem."));
    if (!disponivel) $("#erro-acao").textContent = "Não há cadastros disponíveis para esta ação.";
    formAcao.querySelector("[type=submit]").disabled = !disponivel;
    dialogo.showModal();
}

async function carregar(mensagem = "") {
    dados = await api("/api/painel");
    preencherAbertura();
    renderizarOrdens();
    renderizarFila();
    renderizarCadastros();
    mostrar(mensagem);
}

async function gravar(url, campos, dialogoAberto = false) {
    if (ocupado) return;
    ocupado = true;
    habilitarAbertura();
    $("#atualizar").disabled = true;
    formAcao.querySelector("[type=submit]").disabled = true;
    $("#fechar").disabled = true;
    let resultado;

    try {
        resultado = await api(url, campos);
    } catch (erro) {
        if (dialogoAberto) $("#erro-acao").textContent = erro.message;
        else mostrar(erro.message, true);
    }

    if (resultado) {
        if (dialogoAberto) dialogo.close();
        else formulario.reset();
        try {
            await carregar(resultado.mensagem);
        } catch {
            mostrar(`${resultado.mensagem} Use “Atualizar dados” para recarregar a tela.`, true);
        }
    }

    ocupado = false;
    habilitarAbertura();
    $("#atualizar").disabled = false;
    $("#fechar").disabled = false;
    formAcao.querySelector("[type=submit]").disabled = false;
}

formulario.addEventListener("submit", evento => {
    evento.preventDefault();
    gravar("/api/ordens", new FormData(formulario));
});

formAcao.addEventListener("submit", evento => {
    evento.preventDefault();
    if (!acaoAtual || ocupado) return;
    const campos = new URLSearchParams(new FormData(formAcao));
    for (const [chave, valor] of Object.entries(acaoAtual)) campos.set(chave, valor);
    gravar("/api/ordens/acao", campos, true);
});

$("#fechar").addEventListener("click", () => dialogo.close());
dialogo.addEventListener("cancel", evento => {
    if (ocupado) evento.preventDefault();
});
$("#atualizar").addEventListener("click", async () => {
    if (ocupado) return;
    ocupado = true;
    habilitarAbertura();
    $("#atualizar").disabled = true;
    try {
        await carregar("Dados atualizados.");
    } catch {
        mostrar("Não foi possível carregar os dados. Confira se o servidor está rodando.", true);
    } finally {
        ocupado = false;
        habilitarAbertura();
        $("#atualizar").disabled = false;
    }
});

window.addEventListener("hashchange", navegar);
navegar();
carregar().catch(() => mostrar("Não foi possível carregar os dados do servidor.", true));