const LADOS = {
    AZUL: {
        codigo: "AZUL",
        nome: "Lado azul",
        prefixo: "B"
    },
    VERMELHO: {
        codigo: "VERMELHO",
        nome: "Lado vermelho",
        prefixo: "R"
    }
};

const NOMES_ROTAS = {
    CLASH_LANE: "Clash",
    JUNGLE: "Jungle",
    MID_LANE: "Mid",
    FARM_LANE: "Farm",
    ROAMING: "Roaming"
};

const SEQUENCIA_PICKS = [
    {
        lado: "AZUL",
        indices: [0],
        titulo: "Azul escolhe 1",
        resumo: "B1"
    },
    {
        lado: "VERMELHO",
        indices: [0, 1],
        titulo: "Vermelho escolhe 2",
        resumo: "R1 · R2"
    },
    {
        lado: "AZUL",
        indices: [1, 2],
        titulo: "Azul escolhe 2",
        resumo: "B2 · B3"
    },
    {
        lado: "VERMELHO",
        indices: [2, 3],
        titulo: "Vermelho escolhe 2",
        resumo: "R3 · R4"
    },
    {
        lado: "AZUL",
        indices: [3, 4],
        titulo: "Azul escolhe 2",
        resumo: "B4 · B5"
    },
    {
        lado: "VERMELHO",
        indices: [4],
        titulo: "Vermelho escolhe 1",
        resumo: "R5"
    }
];

const estado = {
    herois: [],
    bans: {
        AZUL: Array(3).fill(null),
        VERMELHO: Array(3).fill(null)
    },
    picks: {
        AZUL: Array(5).fill(null),
        VERMELHO: Array(5).fill(null)
    },
    slotAtual: null,
    inferencia: null,
    inferenciaCarregando: false,
    inferenciaErro: "",
    versaoRequisicaoInferencia: 0
};

const statusApi = document.querySelector("#status-api");
const statusApiTexto = document.querySelector("#status-api-texto");
const meuLadoSelect = document.querySelector("#meu-lado");
const minhaOrdemSelect = document.querySelector("#minha-ordem");
const botaoLimpar = document.querySelector("#botao-limpar");

const faseDraft = document.querySelector("#fase-draft");
const proximaAcao = document.querySelector("#proxima-acao");
const descricaoAcao = document.querySelector("#descricao-acao");
const progressoAtual = document.querySelector("#progresso-atual");
const progressoBarra = document.querySelector("#progresso-barra");
const contadorBans = document.querySelector("#contador-bans");
const contadorPicks = document.querySelector("#contador-picks");

const bansAzul = document.querySelector("#bans-azul");
const bansVermelho = document.querySelector("#bans-vermelho");
const picksAzul = document.querySelector("#picks-azul");
const picksVermelho = document.querySelector("#picks-vermelho");
const sequenciaPicks = document.querySelector("#sequencia-picks");

const resumoProximaAcao = document.querySelector(
    "#resumo-proxima-acao"
);
const resumoProximaDescricao = document.querySelector(
    "#resumo-proxima-descricao"
);
const resumoMinhaPosicao = document.querySelector(
    "#resumo-minha-posicao"
);
const listaFlex = document.querySelector("#lista-flex");
const hipotesesFuncao = document.querySelector("#hipoteses-funcao");

const modalHerois = document.querySelector("#modal-herois");
const modalOverlay = document.querySelector("#modal-overlay");
const modalEtiqueta = document.querySelector("#modal-etiqueta");
const modalTitulo = document.querySelector("#modal-titulo");
const modalDescricao = document.querySelector("#modal-descricao");
const botaoFecharModal = document.querySelector(
    "#botao-fechar-modal"
);
const botaoRemoverHeroi = document.querySelector(
    "#botao-remover-heroi"
);
const pesquisaHeroi = document.querySelector("#pesquisa-heroi");
const filtroRota = document.querySelector("#filtro-rota");
const listaHeroisModal = document.querySelector(
    "#lista-herois-modal"
);

document.addEventListener("DOMContentLoaded", iniciarAplicacao);

botaoLimpar.addEventListener("click", reiniciarDraft);
meuLadoSelect.addEventListener("change", renderizarTudo);
minhaOrdemSelect.addEventListener("change", renderizarTudo);
modalOverlay.addEventListener("click", fecharModal);
botaoFecharModal.addEventListener("click", fecharModal);
botaoRemoverHeroi.addEventListener("click", limparSlotAtual);

pesquisaHeroi.addEventListener("input", renderizarHeroisModal);
filtroRota.addEventListener("change", renderizarHeroisModal);

document.addEventListener("keydown", (evento) => {
    if (evento.key === "Escape") {
        fecharModal();
    }
});

async function iniciarAplicacao() {
    renderizarTudo();

    await Promise.all([
        verificarStatusApi(),
        carregarHerois()
    ]);
}

async function verificarStatusApi() {
    try {
        const resposta = await fetch("/api/status");

        if (!resposta.ok) {
            throw new Error("API indisponível");
        }

        definirStatusApi(true, "API online");
    } catch (erro) {
        definirStatusApi(false, "API indisponível");
    }
}

async function carregarHerois() {
    try {
        const resposta = await fetch("/api/herois");

        if (!resposta.ok) {
            throw new Error("Não foi possível carregar os heróis.");
        }

        estado.herois = await resposta.json();
        renderizarTudo();
        await atualizarInferenciaFuncoes();
    } catch (erro) {
        definirStatusApi(false, "Falha ao carregar heróis");
    }
}

function definirStatusApi(online, texto) {
    statusApi.classList.toggle("status-api--offline", !online);
    statusApiTexto.textContent = texto;
}

function renderizarTudo() {
    const leitura = calcularEstadoDraft();

    bansAzul.innerHTML = criarSlotsBan("AZUL", leitura);
    bansVermelho.innerHTML = criarSlotsBan("VERMELHO", leitura);
    picksAzul.innerHTML = criarSlotsPick("AZUL", leitura);
    picksVermelho.innerHTML = criarSlotsPick("VERMELHO", leitura);
    sequenciaPicks.innerHTML = criarSequenciaPicks(leitura);

    registrarEventosSlots();
    renderizarEstadoGeral(leitura);
    renderizarMinhaPosicao();
    renderizarLeituraFlex();
    renderizarHipotesesFuncao();
}

function criarSlotsBan(lado, leitura) {
    return estado.bans[lado]
        .map((heroi, indice) => {
            const preenchido = Boolean(heroi);
            const ativo = leitura.fase === "BANS" && !preenchido;

            return `
                <button
                    class="
                        slot-ban
                        ${preenchido ? "slot-ban--preenchido" : ""}
                        ${ativo ? "slot-ban--ativo" : ""}
                    "
                    type="button"
                    data-tipo="BAN"
                    data-lado="${lado}"
                    data-indice="${indice}"
                >
                    <span class="slot-ban__ordem">
                        Ban ${indice + 1}
                    </span>

                    <span class="slot-ban__nome">
                        ${
                            preenchido
                                ? escaparHtml(heroi.nome)
                                : "Selecionar ban"
                        }
                    </span>
                </button>
            `;
        })
        .join("");
}

function criarSlotsPick(lado, leitura) {
    const meuLado = meuLadoSelect.value;
    const minhaOrdem = Number(minhaOrdemSelect.value);

    return estado.picks[lado]
        .map((heroi, indice) => {
            const preenchido = Boolean(heroi);
            const ativo = slotPertenceRodadaAtual(
                lado,
                indice,
                leitura
            );
            const meuSlot =
                meuLado === lado && minhaOrdem === indice + 1;
            const rodadaSlot = buscarRodadaDoSlot(lado, indice);
            const bloqueado =
                leitura.fase === "BANS"
                || (
                    leitura.fase === "PICKS"
                    && rodadaSlot > leitura.indiceRodadaAtual
                    && !preenchido
                );

            return `
                <button
                    class="
                        slot-pick
                        ${preenchido ? "slot-pick--preenchido" : ""}
                        ${ativo ? "slot-pick--ativo" : ""}
                        ${meuSlot ? "slot-pick--meu" : ""}
                        ${bloqueado ? "slot-pick--bloqueado" : ""}
                    "
                    type="button"
                    data-tipo="PICK"
                    data-lado="${lado}"
                    data-indice="${indice}"
                >
                    <span class="slot-pick__numero">
                        ${LADOS[lado].prefixo}${indice + 1}
                    </span>

                    <span class="slot-pick__conteudo">
                        <span class="slot-pick__ordem">
                            Jogador ${indice + 1}
                            · Rodada ${rodadaSlot + 1}
                        </span>

                        <span class="slot-pick__nome">
                            ${
                                preenchido
                                    ? escaparHtml(heroi.nome)
                                    : ativo
                                        ? "Escolha atual"
                                        : "Aguardando pick"
                            }
                        </span>

                        ${
                            preenchido
                                ? criarChipsRotas(heroi)
                                : ""
                        }
                    </span>

                    <span class="slot-pick__acao">
                        ${preenchido ? "↻" : "+"}
                    </span>
                </button>
            `;
        })
        .join("");
}

function criarSequenciaPicks(leitura) {
    return SEQUENCIA_PICKS
        .map((rodada, indice) => {
            const concluida = rodadaConcluida(indice);
            const ativa =
                leitura.fase === "PICKS"
                && leitura.indiceRodadaAtual === indice;

            return `
                <article
                    class="
                        rodada-pick
                        rodada-pick--${rodada.lado.toLowerCase()}
                        ${ativa ? "rodada-pick--ativa" : ""}
                        ${concluida ? "rodada-pick--concluida" : ""}
                    "
                >
                    <span class="rodada-pick__numero">
                        ${indice + 1}
                    </span>

                    <div class="rodada-pick__texto">
                        <strong>${rodada.titulo}</strong>
                        <span>${rodada.resumo}</span>
                    </div>
                </article>
            `;
        })
        .join("");
}

function registrarEventosSlots() {
    document
        .querySelectorAll("[data-tipo][data-lado][data-indice]")
        .forEach((slot) => {
            slot.addEventListener("click", () => {
                abrirModal(
                    slot.dataset.tipo,
                    slot.dataset.lado,
                    Number(slot.dataset.indice)
                );
            });
        });
}

function abrirModal(tipo, lado, indice) {
    estado.slotAtual = {
        tipo,
        lado,
        indice
    };

    const heroiAtual = obterHeroiSlot(tipo, lado, indice);
    const codigoSlot =
        tipo === "BAN"
            ? `Ban ${indice + 1}`
            : `${LADOS[lado].prefixo}${indice + 1}`;

    modalEtiqueta.textContent =
        tipo === "BAN" ? "Registrar ban" : "Registrar pick";
    modalTitulo.textContent = `${LADOS[lado].nome} · ${codigoSlot}`;
    modalDescricao.textContent =
        tipo === "BAN"
            ? "Escolha o herói removido por este lado."
            : "A função ainda não é fixa. Heróis flex exibem todas as possibilidades conhecidas.";

    pesquisaHeroi.value = "";
    filtroRota.value = "";
    botaoRemoverHeroi.classList.toggle("oculto", !heroiAtual);

    renderizarHeroisModal();

    modalHerois.classList.remove("oculto");
    modalHerois.setAttribute("aria-hidden", "false");
    pesquisaHeroi.focus();
}

function fecharModal() {
    modalHerois.classList.add("oculto");
    modalHerois.setAttribute("aria-hidden", "true");
    estado.slotAtual = null;
}

function renderizarHeroisModal() {
    if (!estado.slotAtual) {
        return;
    }

    const termo = normalizarTexto(pesquisaHeroi.value);
    const rotaFiltrada = filtroRota.value;

    const heroisFiltrados = [...estado.herois]
        .filter((heroi) =>
            normalizarTexto(heroi.nome).includes(termo)
        )
        .filter((heroi) =>
            !rotaFiltrada
            || obterRotasHeroi(heroi).includes(rotaFiltrada)
        )
        .sort((heroiA, heroiB) =>
            heroiA.nome.localeCompare(heroiB.nome, "pt-BR")
        );

    if (heroisFiltrados.length === 0) {
        listaHeroisModal.innerHTML = `
            <div class="sem-resultados">
                Nenhum herói encontrado com os filtros atuais.
            </div>
        `;
        return;
    }

    listaHeroisModal.innerHTML = heroisFiltrados
        .map((heroi) => {
            const indisponivel = heroiEstaEmOutroSlot(heroi.id);
            const flex = obterRotasHeroi(heroi).length > 1;

            return `
                <button
                    class="
                        heroi-opcao
                        ${flex ? "heroi-opcao--flex" : ""}
                    "
                    type="button"
                    data-heroi-id="${heroi.id}"
                    ${indisponivel ? "disabled" : ""}
                >
                    <strong>${escaparHtml(heroi.nome)}</strong>
                    <span>${escaparHtml(heroi.estilo)}</span>

                    <div class="heroi-opcao__rotas">
                        ${criarChipsRotas(heroi, true)}
                    </div>
                </button>
            `;
        })
        .join("");

    listaHeroisModal
        .querySelectorAll(".heroi-opcao:not(:disabled)")
        .forEach((botao) => {
            botao.addEventListener("click", () => {
                selecionarHeroi(Number(botao.dataset.heroiId));
            });
        });
}

function selecionarHeroi(heroiId) {
    if (!estado.slotAtual) {
        return;
    }

    const heroi = estado.herois.find(
        (item) => Number(item.id) === Number(heroiId)
    );

    if (!heroi) {
        return;
    }

    const { tipo, lado, indice } = estado.slotAtual;
    const colecao = tipo === "BAN" ? estado.bans : estado.picks;

    colecao[lado][indice] = heroi;

    fecharModal();
    renderizarTudo();

    if (tipo === "PICK") {
        atualizarInferenciaFuncoes();
    }
}

function limparSlotAtual() {
    if (!estado.slotAtual) {
        return;
    }

    const { tipo, lado, indice } = estado.slotAtual;
    const colecao = tipo === "BAN" ? estado.bans : estado.picks;

    colecao[lado][indice] = null;

    fecharModal();
    renderizarTudo();

    if (tipo === "PICK") {
        atualizarInferenciaFuncoes();
    }
}

function obterHeroiSlot(tipo, lado, indice) {
    const colecao = tipo === "BAN" ? estado.bans : estado.picks;
    return colecao[lado][indice];
}

function heroiEstaEmOutroSlot(heroiId) {
    const atual = estado.slotAtual;

    const slots = [
        ...listarSlots("BAN", "AZUL"),
        ...listarSlots("BAN", "VERMELHO"),
        ...listarSlots("PICK", "AZUL"),
        ...listarSlots("PICK", "VERMELHO")
    ];

    return slots.some((slot) => {
        if (!slot.heroi) {
            return false;
        }

        const mesmoSlot =
            atual
            && atual.tipo === slot.tipo
            && atual.lado === slot.lado
            && atual.indice === slot.indice;

        return !mesmoSlot
            && Number(slot.heroi.id) === Number(heroiId);
    });
}

function listarSlots(tipo, lado) {
    const colecao = tipo === "BAN" ? estado.bans : estado.picks;

    return colecao[lado].map((heroi, indice) => ({
        tipo,
        lado,
        indice,
        heroi
    }));
}

function calcularEstadoDraft() {
    const quantidadeBans = contarPreenchidos(estado.bans);
    const quantidadePicks = contarPreenchidos(estado.picks);
    const indiceRodadaAtual = encontrarRodadaAtual();

    if (quantidadeBans < 6) {
        return {
            fase: "BANS",
            quantidadeBans,
            quantidadePicks,
            indiceRodadaAtual: -1,
            totalAcoes: quantidadeBans + quantidadePicks,
            titulo: "Registre os três bans de cada equipe",
            descricao:
                `Faltam ${6 - quantidadeBans} bans para iniciar a fase de picks.`
        };
    }

    if (quantidadePicks < 10) {
        const rodada = SEQUENCIA_PICKS[indiceRodadaAtual];
        const faltantes = rodada.indices.filter(
            (indice) => !estado.picks[rodada.lado][indice]
        );

        return {
            fase: "PICKS",
            quantidadeBans,
            quantidadePicks,
            indiceRodadaAtual,
            totalAcoes: quantidadeBans + quantidadePicks,
            titulo: rodada.titulo,
            descricao:
                `Preencha ${faltantes.map((indice) =>
                    `${LADOS[rodada.lado].prefixo}${indice + 1}`
                ).join(" e ")}.`
        };
    }

    return {
        fase: "CONCLUIDO",
        quantidadeBans,
        quantidadePicks,
        indiceRodadaAtual: -1,
        totalAcoes: 16,
        titulo: "Draft completo",
        descricao:
            "Os dez picks e os seis bans foram registrados."
    };
}

function encontrarRodadaAtual() {
    return SEQUENCIA_PICKS.findIndex(
        (_, indice) => !rodadaConcluida(indice)
    );
}

function rodadaConcluida(indiceRodada) {
    const rodada = SEQUENCIA_PICKS[indiceRodada];

    return rodada.indices.every(
        (indice) => Boolean(estado.picks[rodada.lado][indice])
    );
}

function slotPertenceRodadaAtual(lado, indice, leitura) {
    if (leitura.fase !== "PICKS") {
        return false;
    }

    const rodada = SEQUENCIA_PICKS[leitura.indiceRodadaAtual];

    return rodada.lado === lado
        && rodada.indices.includes(indice)
        && !estado.picks[lado][indice];
}

function buscarRodadaDoSlot(lado, indice) {
    return SEQUENCIA_PICKS.findIndex(
        (rodada) =>
            rodada.lado === lado && rodada.indices.includes(indice)
    );
}

function renderizarEstadoGeral(leitura) {
    faseDraft.textContent =
        leitura.fase === "BANS"
            ? "Fase de bans"
            : leitura.fase === "PICKS"
                ? `Fase de picks · Rodada ${leitura.indiceRodadaAtual + 1}`
                : "Draft concluído";

    proximaAcao.textContent = leitura.titulo;
    descricaoAcao.textContent = leitura.descricao;
    progressoAtual.textContent = leitura.totalAcoes;
    progressoBarra.style.width =
        `${(leitura.totalAcoes / 16) * 100}%`;

    contadorBans.textContent =
        `${leitura.quantidadeBans} de 6 bans`;
    contadorPicks.textContent =
        `${leitura.quantidadePicks} de 10 picks`;

    resumoProximaAcao.textContent = leitura.titulo;
    resumoProximaDescricao.textContent = leitura.descricao;
}

function renderizarMinhaPosicao() {
    const meuLado = meuLadoSelect.value;
    const minhaOrdem = Number(minhaOrdemSelect.value);

    if (meuLado === "INDEFINIDO" || !minhaOrdem) {
        resumoMinhaPosicao.textContent = "Não definida";
        return;
    }

    const rodada = buscarRodadaDoSlot(meuLado, minhaOrdem - 1);

    resumoMinhaPosicao.textContent =
        `${LADOS[meuLado].nome} · `
        + `${LADOS[meuLado].prefixo}${minhaOrdem} · `
        + `Rodada ${rodada + 1}`;
}

function renderizarLeituraFlex() {
    const flexSelecionados = [
        ...listarSlots("PICK", "AZUL"),
        ...listarSlots("PICK", "VERMELHO")
    ].filter((slot) =>
        slot.heroi && obterRotasHeroi(slot.heroi).length > 1
    );

    if (flexSelecionados.length === 0) {
        listaFlex.innerHTML = `
            <p>Nenhum herói flex selecionado.</p>
        `;
        return;
    }

    listaFlex.innerHTML = flexSelecionados
        .map((slot) => `
            <div class="flex-item">
                <strong>
                    ${LADOS[slot.lado].prefixo}${slot.indice + 1}
                    · ${escaparHtml(slot.heroi.nome)}
                </strong>

                <div>${criarChipsRotas(slot.heroi, true)}</div>
            </div>
        `)
        .join("");
}

async function atualizarInferenciaFuncoes() {
    const versaoAtual = ++estado.versaoRequisicaoInferencia;

    estado.inferenciaCarregando = true;
    estado.inferenciaErro = "";
    renderizarHipotesesFuncao();

    const corpo = {
        picksAzul: montarPicksInferencia("AZUL"),
        picksVermelho: montarPicksInferencia("VERMELHO")
    };

    try {
        const resposta = await fetch("/api/draft/inferir-funcoes", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(corpo)
        });

        if (!resposta.ok) {
            throw new Error(await extrairMensagemErro(resposta));
        }

        const inferencia = await resposta.json();

        if (versaoAtual !== estado.versaoRequisicaoInferencia) {
            return;
        }

        estado.inferencia = inferencia;
    } catch (erro) {
        if (versaoAtual !== estado.versaoRequisicaoInferencia) {
            return;
        }

        estado.inferencia = null;
        estado.inferenciaErro = erro.message;
    } finally {
        if (versaoAtual === estado.versaoRequisicaoInferencia) {
            estado.inferenciaCarregando = false;
            renderizarHipotesesFuncao();
        }
    }
}

function montarPicksInferencia(lado) {
    return estado.picks[lado]
        .map((heroi, indice) => {
            if (!heroi) {
                return null;
            }

            return {
                ordem: indice + 1,
                heroiId: Number(heroi.id)
            };
        })
        .filter(Boolean);
}

function renderizarHipotesesFuncao() {
    if (estado.inferenciaCarregando) {
        hipotesesFuncao.innerHTML = `
            <div class="inferencia-carregando">
                <span class="inferencia-carregando__circulo"></span>
                Calculando combinações válidas de função...
            </div>
        `;
        return;
    }

    if (estado.inferenciaErro) {
        hipotesesFuncao.innerHTML = `
            <div class="inferencia-erro">
                ${escaparHtml(estado.inferenciaErro)}
            </div>
        `;
        return;
    }

    if (!estado.inferencia) {
        hipotesesFuncao.innerHTML = `
            <p>
                As possibilidades aparecerão conforme os heróis forem escolhidos.
            </p>
        `;
        return;
    }

    hipotesesFuncao.innerHTML = `
        <div class="inferencia-versao">
            Motor ${escaparHtml(estado.inferencia.versaoMotor)}
        </div>

        <div class="inferencia-equipes">
            ${criarPainelInferenciaEquipe(estado.inferencia.equipeAzul)}
            ${criarPainelInferenciaEquipe(estado.inferencia.equipeVermelha)}
        </div>
    `;
}

function criarPainelInferenciaEquipe(equipe) {
    const lado = equipe.lado;
    const classeLado = lado.toLowerCase();

    if (equipe.totalPicks === 0) {
        return `
            <section class="inferencia-equipe inferencia-equipe--${classeLado}">
                <header class="inferencia-equipe__cabecalho">
                    <strong>${LADOS[lado].nome}</strong>
                    <span>Sem picks</span>
                </header>

                <p>Nenhuma função pode ser inferida ainda.</p>
            </section>
        `;
    }

    if (!equipe.composicaoCompativel) {
        return `
            <section class="inferencia-equipe inferencia-equipe--${classeLado}">
                <header class="inferencia-equipe__cabecalho">
                    <strong>${LADOS[lado].nome}</strong>
                    <span class="confianca confianca--incompativel">
                        Incompatível
                    </span>
                </header>

                <div class="inferencia-conflito">
                    Não existe distribuição válida sem repetir função.
                </div>

                ${criarListaAvisosInferencia(equipe.avisos)}
            </section>
        `;
    }

    const principal = equipe.hipoteses?.[0];
    const ambiguidades = new Map(
        (equipe.ambiguidades ?? []).map((item) => [item.slot, item])
    );

    const atribuicoes = principal?.atribuicoes ?? [];
    const atribuicoesHtml = atribuicoes
        .map((atribuicao) => {
            const ambiguidade = ambiguidades.get(atribuicao.slot);
            const confirmada = ambiguidade?.funcaoConfirmada;

            return `
                <div class="atribuicao-inferida">
                    <div>
                        <strong>
                            ${escaparHtml(atribuicao.slot)}
                            · ${escaparHtml(atribuicao.heroi)}
                        </strong>

                        <span>
                            ${atribuirNomeRota(atribuicao.rota)}
                        </span>
                    </div>

                    <span
                        class="
                            estado-funcao
                            ${
                                confirmada
                                    ? "estado-funcao--confirmada"
                                    : "estado-funcao--hipotese"
                            }
                        "
                    >
                        ${confirmada ? "Confirmada" : "Hipótese"}
                    </span>
                </div>
            `;
        })
        .join("");

    const rotasAbertas = principal?.rotasAbertas ?? [];
    const rotasAbertasHtml = rotasAbertas.length > 0
        ? rotasAbertas
            .map((rota) => `
                <span class="rota-chip">
                    ${atribuirNomeRota(rota)}
                </span>
            `)
            .join("")
        : `<span class="rotas-completas">Todas as funções preenchidas</span>`;

    return `
        <section class="inferencia-equipe inferencia-equipe--${classeLado}">
            <header class="inferencia-equipe__cabecalho">
                <div>
                    <strong>${LADOS[lado].nome}</strong>
                    <small>
                        ${equipe.totalHipoteses} hipótese(s) válida(s)
                    </small>
                </div>

                <span class="confianca confianca--${normalizarTexto(
                    equipe.confiancaMelhorHipotese
                )}">
                    Confiança ${escaparHtml(equipe.confiancaMelhorHipotese)}
                </span>
            </header>

            <div class="inferencia-equipe__pontuacao">
                <span>Melhor hipótese</span>
                <strong>${principal?.pontuacaoAfinidade ?? 0}/100</strong>
            </div>

            <div class="atribuicoes-inferidas">
                ${atribuicoesHtml}
            </div>

            <div class="rotas-abertas">
                <span>Funções ainda abertas</span>
                <div>${rotasAbertasHtml}</div>
            </div>

            ${criarListaAvisosInferencia(equipe.avisos)}
        </section>
    `;
}

function criarListaAvisosInferencia(avisos = []) {
    if (avisos.length === 0) {
        return "";
    }

    return `
        <ul class="avisos-inferencia">
            ${avisos
                .map((aviso) => `<li>${escaparHtml(aviso)}</li>`)
                .join("")}
        </ul>
    `;
}

async function extrairMensagemErro(resposta) {
    try {
        const erro = await resposta.json();

        if (Array.isArray(erro.detalhes) && erro.detalhes.length > 0) {
            return erro.detalhes.join(" ");
        }

        return erro.erro || "Não foi possível inferir as funções.";
    } catch (erro) {
        return "Não foi possível inferir as funções.";
    }
}

function criarChipsRotas(heroi, incluirFlex = false) {
    const rotas = obterRotasHeroi(heroi);
    const chipsRotas = rotas
        .map((rota) => `
            <span class="rota-chip">
                ${escaparHtml(NOMES_ROTAS[rota] ?? rota)}
            </span>
        `)
        .join("");

    const chipFlex = incluirFlex && rotas.length > 1
        ? `<span class="flex-chip">Flex</span>`
        : "";

    return `
        <span class="slot-pick__rotas">
            ${chipsRotas}
            ${chipFlex}
        </span>
    `;
}

function obterRotasHeroi(heroi) {
    if (
        Array.isArray(heroi.rotasPossiveis)
        && heroi.rotasPossiveis.length > 0
    ) {
        return heroi.rotasPossiveis;
    }

    return heroi.rota ? [heroi.rota] : [];
}

function atribuirNomeRota(rota) {
    return escaparHtml(NOMES_ROTAS[rota] ?? rota);
}

function contarPreenchidos(colecaoPorLado) {
    return Object.values(colecaoPorLado)
        .flat()
        .filter(Boolean)
        .length;
}

function reiniciarDraft() {
    estado.bans.AZUL = Array(3).fill(null);
    estado.bans.VERMELHO = Array(3).fill(null);
    estado.picks.AZUL = Array(5).fill(null);
    estado.picks.VERMELHO = Array(5).fill(null);
    estado.inferencia = null;
    estado.inferenciaErro = "";

    meuLadoSelect.value = "INDEFINIDO";
    minhaOrdemSelect.value = "";

    renderizarTudo();
    atualizarInferenciaFuncoes();
}

function normalizarTexto(valor) {
    return String(valor ?? "")
        .normalize("NFD")
        .replace(/\p{M}/gu, "")
        .trim()
        .toLowerCase();
}

function escaparHtml(valor) {
    const elemento = document.createElement("div");
    elemento.textContent = String(valor);
    return elemento.innerHTML;
}
