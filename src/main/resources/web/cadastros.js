"use strict";

const CadastrosGerais = (() => {
    let filtroTecnicos = false;
    let enviando = false;

    function renderizar() {
        const lista = $("#lista-cadastros");
        lista.replaceChildren();

        grupo(lista, "Equipamentos", "Novo equipamento", () => editarEquipamento(),
            dados.equipamentos, e => e.nome,
            e => [
                `${e.identificador} · ${e.tipo} · ${rotulo(e.status)}`,
                e.descricao
            ],
            e => [
                ["Editar", () => editarEquipamento(e)],
                [e.ativo ? "Inativar" : "Ativar", () => confirmar(
                    e.ativo ? "Inativar equipamento" : "Ativar equipamento",
                    e.nome,
                    e.ativo ? "inativar-equipamento" : "ativar-equipamento", e.id
                )],
                ["Excluir", () => confirmar("Excluir equipamento",
                    `Confirme a exclusão de ${e.nome}.`,
                    "excluir-equipamento", e.id)]
            ]);

        grupo(lista, "Usuários", "Novo usuário", () => editarUsuario(),
            dados.usuarios, u => u.nome,
            u => [`Login: ${u.login} · ${rotulo(u.perfil)}`],
            u => [
                ["Editar", () => editarUsuario(u)],
                ["Alterar perfil", () => abrir(
                    `Alterar perfil · ${u.nome}`, "alterar-perfil", u.id,
                    selectCampo("perfil", "Perfil", dados.perfis, u.perfil)
                )]
            ]);

        const tecnicos = filtroTecnicos
            ? dados.tecnicos.filter(t => t.disponibilidade === "DISPONIVEL")
            : dados.tecnicos;

        const painel = grupo(lista, "Técnicos", "Vincular técnico", vincularTecnico,
            tecnicos, t => t.nome,
            t => [`${rotulo(t.disponibilidade)} · ${t.fila.length} na fila`],
            t => [["Alterar disponibilidade", () => abrir(
                `Disponibilidade · ${t.nome}`, "alterar-disponibilidade", t.id,
                selectCampo("disponibilidade", "Disponibilidade",
                    dados.disponibilidades, t.disponibilidade)
            )]]);

        const filtro = criar("label", "Mostrar apenas disponíveis", "cad-filtro");
        const caixa = criar("input");
        caixa.type = "checkbox";
        caixa.checked = filtroTecnicos;
        caixa.addEventListener("change", () => {
            filtroTecnicos = caixa.checked;
            renderizar();
            $("#lista-cadastros .cad-filtro input").focus();
        });
        filtro.prepend(caixa);
        painel.children[0].after(filtro);
    }

    function botao(texto, clique, classe = "secundario") {
        const b = criar("button", texto, classe);
        b.type = "button";
        b.addEventListener("click", clique);
        return b;
    }

    function grupo(lista, titulo, textoNovo, novo, registros, nome, detalhes, acoes) {
        const painel = criar("section", "", "painel");
        const topo = criar("div", "", "linha");
        topo.append(criar("h2", titulo), botao(textoNovo, novo, ""));
        painel.append(topo);

        if (!registros.length) {
            painel.append(criar("p", "Nenhum registro para exibir.", "meta"));
        }

        for (const registro of registros) {
            const linha = criar("div", "", "cad-registro");
            const ficha = criar("details");
            ficha.append(criar("summary", nome(registro)));
            for (const item of detalhes(registro)) ficha.append(criar("p", item, "meta"));

            const botoes = criar("div", "", "botoes");
            for (const [texto, executar] of acoes(registro)) {
                botoes.append(botao(texto, executar));
            }

            linha.append(ficha, botoes);
            painel.append(linha);
        }
        lista.append(painel);
        return painel;
    }

    function textoCampo(nome, titulo, limite, valor = "", longo = false) {
        const label = criar("label", titulo);
        const entrada = criar(longo ? "textarea" : "input");
        entrada.name = nome;
        entrada.value = valor;
        entrada.maxLength = limite;
        entrada.required = true;
        if (longo) entrada.rows = 3;
        label.append(entrada);
        return label;
    }

    function selectCampo(nome, titulo, valores, atual = "") {
        const label = criar("label", titulo);
        const select = criar("select");
        select.name = nome;
        select.required = true;
        select.add(new Option("Selecione", ""));
        for (const valor of valores) {
            const item = typeof valor === "string"
                ? { id: valor, nome: rotulo(valor) } : valor;
            select.add(new Option(item.nome, item.id));
        }
        select.value = atual;
        label.append(select);
        return label;
    }

    function editarEquipamento(e) {
        abrir(e ? `Editar · ${e.nome}` : "Novo equipamento",
            e ? "editar-equipamento" : "criar-equipamento", e?.id,
            textoCampo("nome", "Nome", 120, e?.nome),
            textoCampo("tipo", "Tipo", 80, e?.tipo),
            textoCampo("identificador", "Identificador", 80, e?.identificador),
            textoCampo("descricao", "Descrição", 500, e?.descricao, true));
    }

    function editarUsuario(u) {
        const campos = [
            textoCampo("nome", "Nome", 120, u?.nome),
            textoCampo("login", "Login", 80, u?.login)
        ];
        if (!u) campos.push(selectCampo("perfil", "Perfil", dados.perfis));
        abrir(u ? `Editar · ${u.nome}` : "Novo usuário",
            u ? "editar-usuario" : "criar-usuario", u?.id, ...campos);
    }

    function vincularTecnico() {
        const vinculados = new Set(dados.tecnicos.map(t => t.usuarioId));
        const elegiveis = dados.usuarios.filter(u =>
            u.perfil === "TECNICO" && !vinculados.has(u.id));

        if (!elegiveis.length) {
            mostrar("Cadastre primeiro um usuário com perfil Técnico e sem vínculo técnico.", true);
            return;
        }

        abrir("Vincular técnico", "criar-tecnico", null,
            selectCampo("usuarioId", "Usuário técnico", elegiveis,
                elegiveis.length === 1 ? elegiveis[0].id : ""));
    }

    function confirmar(titulo, mensagem, acao, id) {
        abrir(titulo, acao, id, criar("p", mensagem));
    }

    function abrir(titulo, acao, id, ...campos) {
        if (ocupado || enviando) return;

        const modal = criar("dialog");
        const cabecalho = criar("h2", titulo);
        cabecalho.id = "titulo-cadastro-modal";
        modal.setAttribute("aria-labelledby", cabecalho.id);

        const form = criar("form");
        form.append(...campos);

        const acoes = criar("div", "", "botoes");
        const voltar = botao("Voltar", () => modal.close());
        const salvar = criar("button", "Confirmar");
        salvar.type = "submit";
        acoes.append(voltar, salvar);
        const erro = criar("p", "", "erro");
        erro.setAttribute("role", "alert");
        form.append(acoes, erro);
        modal.append(cabecalho, form);
        document.body.append(modal);
        modal.addEventListener("close", () => modal.remove(), { once: true });
        modal.addEventListener("cancel", evento => {
            if (enviando) evento.preventDefault();
        });

        form.addEventListener("submit", async evento => {
            evento.preventDefault();
            if (enviando || ocupado) return;
            enviando = true;
            salvar.disabled = true;
            voltar.disabled = true;
            erro.textContent = "";

            const valores = new URLSearchParams(new FormData(form));
            valores.set("acao", acao);
            if (id != null) valores.set("id", id);

            let resultado;
            try {
                resultado = await api("/api/cadastros", valores);
            } catch (falha) {
                erro.textContent = falha.message;
            } finally {
                enviando = false;
                salvar.disabled = false;
                voltar.disabled = false;
            }

            if (!resultado) return;
            modal.close();
            ocupado = true;
            habilitarAbertura();
            $("#atualizar").disabled = true;
            try {
                await carregar(resultado.mensagem);
            } catch {
                mostrar(`${resultado.mensagem} Use “Atualizar dados” para recarregar a tela.`, true);
            } finally {
                ocupado = false;
                habilitarAbertura();
                $("#atualizar").disabled = false;
            }
        });

        modal.showModal();
    }

    return { renderizar };
})();