const ROTAS = [
    {
        codigo: "CLASH_LANE",
        nome: "Clash Lane",
        sigla: "CL"
    },
    {
        codigo: "JUNGLE",
        nome: "Jungle",
        sigla: "JG"
    },
    {
        codigo: "MID_LANE",
        nome: "Mid Lane",
        sigla: "MID"
    },
    {
        codigo: "FARM_LANE",
        nome: "Farm Lane",
        sigla: "ADC"
    },
    {
        codigo: "ROAMING",
        nome: "Roaming",
        sigla: "SUP"
    }
];

const ROTAS_POR_CODIGO = Object.fromEntries(
    ROTAS.map((rota) => [rota.codigo, rota])
);

const ROTULOS_COMPONENTES = {
    base: "Base",
    confronto: "Confronto",
    sinergia: "Sinergia",
    composicao: "Composição",
    respostaAosInimigos: "Resposta",
    acessibilidade: "Execução"
};

const estado = {
    herois: [],
    aliados: Array(5).fill(null),
    inimigos: Array(5).fill(null),
    slotAtual: null,
    analisando: false
};

const slotsAliados = document.querySelector("#slots-aliados");
const slotsInimigos = document.querySelector("#slots-inimigos");

const contadorDraft = document.querySelector("#contador-draft");
const botaoLimpar = document.querySelector("#botao-limpar");
const botaoAnalisar = document.querySelector("#botao-analisar");
const rotaAlvoSelect = document.querySelector("#rota-alvo");

const statusApi = document.querySelector("#status-api");
const statusApiTexto = document.querySelector("#status-api-texto");

const mensagemErro = document.querySelector("#mensagem-erro");
const estadoInicial = document.querySelector("#estado-inicial");
const carregamento = document.querySelector("#carregamento");
const resultadoAnalise = document.querySelector("#resultado-analise");

const rotaAnalisada = document.querySelector("#rota-analisada");
const versaoDados = document.querySelector("#versao-dados");
const quantidadeResultados = document.querySelector(
    "#quantidade-resultados"
);
const listaRecomendacoes = document.querySelector(
    "#lista-recomendacoes"
);
const painelAvisos = document.querySelector("#painel-avisos");
const listaAvisos = document.querySelector("#lista-avisos");

const modalHerois = document.querySelector("#modal-herois");
const modalOverlay = document.querySelector("#modal-overlay");
const botaoFecharModal = document.querySelector(
    "#botao-fechar-modal"
);
const botaoRemoverHeroi = document.querySelector(
    "#botao-remover-heroi"
);
const modalTitulo = document.querySelector("#modal-titulo");
const modalDescricao = document.querySelector("#modal-descricao");
const pesquisaHeroi = document.querySelector("#pesquisa-heroi");
const listaHeroisModal = document.querySelector(
    "#lista-herois-modal"
);

document.addEventListener("DOMContentLoaded", iniciarAplicacao);

botaoLimpar.addEventListener("click", limparDraft);
botaoAnalisar.addEventListener("click", analisarDraft);
botaoRemoverHeroi.addEventListener("click", removerHeroiAtual);

rotaAlvoSelect.addEventListener("change", () => {
    limparErro();
    limparAnaliseAnterior();
    renderizarDraft();
});

botaoFecharModal.addEventListener("click", fecharModal);
modalOverlay.addEventListener("click", fecharModal);

pesquisaHeroi.addEventListener("input", () => {
    renderizarHeroisDoModal(pesquisaHeroi.value);
});

document.addEventListener("keydown", (evento) => {
    if (evento.key === "Escape") {
        fecharModal();
    }
});

async function iniciarAplicacao() {
    renderizarDraft();
    atualizarBotaoAnalise();

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
        atualizarBotaoAnalise();
    } catch (erro) {
        mostrarErro(erro.message);
        atualizarBotaoAnalise();
    }
}

function definirStatusApi(online, texto) {
    statusApi.classList.toggle("status-api--offline", !online);
    statusApiTexto.textContent = texto;
}

function renderizarDraft() {
    slotsAliados.innerHTML = criarSlots("aliados");
    slotsInimigos.innerHTML = criarSlots("inimigos");

    adicionarEventosAosSlots();
    atualizarContador();
}

function criarSlots(equipe) {
    const rotaAlvo = rotaAlvoSelect.value;

    return ROTAS.map((rota, indice) => {
        const heroi = estado[equipe][indice];
        const preenchido = Boolean(heroi);
        const slotAlvo =
            equipe === "aliados" && rota.codigo === rotaAlvo;
        const alvoVazio = slotAlvo && !preenchido;
        const alvoPreenchido = slotAlvo && preenchido;

        return `
            <button
                class="
                    slot-heroi
                    ${preenchido ? "slot-heroi--preenchido" : ""}
                    ${alvoVazio ? "slot-heroi--alvo" : ""}
                    ${alvoPreenchido ? "slot-heroi--alvo-invalido" : ""}
                "
                type="button"
                data-equipe="${equipe}"
                data-indice="${indice}"
                title="Selecionar herói para ${rota.nome}"
            >
                <span class="slot-heroi__icone">
                    ${
                        preenchido
                            ? obterIniciais(heroi.nome)
                            : rota.sigla
                    }
                </span>

                <span class="slot-heroi__conteudo">
                    <span class="slot-heroi__rota">
                        ${rota.nome}
                    </span>

                    <span class="slot-heroi__nome">
                        ${
                            preenchido
                                ? escaparHtml(heroi.nome)
                                : alvoVazio
                                    ? "Slot da recomendação"
                                    : "Selecionar herói"
                        }
                    </span>
                </span>

                <span class="slot-heroi__acao">
                    ${
                        alvoVazio
                            ? "ALVO"
                            : preenchido
                                ? "↻"
                                : "+"
                    }
                </span>
            </button>
        `;
    }).join("");
}

function adicionarEventosAosSlots() {
    document
        .querySelectorAll(".slot-heroi")
        .forEach((slot) => {
            slot.addEventListener("click", () => {
                const equipe = slot.dataset.equipe;
                const indice = Number(slot.dataset.indice);

                abrirModal(equipe, indice);
            });
        });
}

function abrirModal(equipe, indice) {
    estado.slotAtual = {
        equipe,
        indice
    };

    const rota = ROTAS[indice];
    const heroiAtual = estado[equipe][indice];
    const nomeEquipe =
        equipe === "aliados"
            ? "Equipe aliada"
            : "Equipe inimiga";

    modalTitulo.textContent = rota.nome;

    modalDescricao.textContent =
        `${nomeEquipe} · Escolha entre os heróis de ${rota.nome}.`;

    pesquisaHeroi.value = "";

    botaoRemoverHeroi.classList.toggle(
        "oculto",
        !heroiAtual
    );

    renderizarHeroisDoModal();

    modalHerois.classList.remove("oculto");
    modalHerois.setAttribute("aria-hidden", "false");

    pesquisaHeroi.focus();
}

function fecharModal() {
    modalHerois.classList.add("oculto");
    modalHerois.setAttribute("aria-hidden", "true");

    estado.slotAtual = null;
}

function renderizarHeroisDoModal(filtro = "") {
    if (!estado.slotAtual) {
        return;
    }

    const termo = normalizarTexto(filtro);
    const rota = ROTAS[estado.slotAtual.indice];

    const heroisFiltrados = estado.herois
        .filter((heroi) => heroi.rota === rota.codigo)
        .filter((heroi) =>
            normalizarTexto(heroi.nome).includes(termo)
        )
        .sort((heroiA, heroiB) =>
            heroiA.nome.localeCompare(heroiB.nome, "pt-BR")
        );

    if (heroisFiltrados.length === 0) {
        listaHeroisModal.innerHTML = `
            <div class="sem-resultados">
                Nenhum herói de ${escaparHtml(rota.nome)} encontrado.
            </div>
        `;

        return;
    }

    listaHeroisModal.innerHTML = heroisFiltrados
        .map((heroi) => {
            const selecionadoEmOutroSlot =
                heroiSelecionadoEmOutroSlot(heroi.id);

            const atributos = heroi.atributos
                ? `Controle ${heroi.atributos.controle} · `
                    + `Mobilidade ${heroi.atributos.mobilidade}`
                : heroi.estilo;

            return `
                <button
                    class="heroi-opcao"
                    type="button"
                    data-heroi-id="${heroi.id}"
                    ${selecionadoEmOutroSlot ? "disabled" : ""}
                >
                    <strong>
                        ${escaparHtml(heroi.nome)}
                    </strong>

                    <span>
                        ${escaparHtml(heroi.estilo)}
                    </span>

                    <small>
                        ${escaparHtml(atributos)}
                    </small>
                </button>
            `;
        })
        .join("");

    listaHeroisModal
        .querySelectorAll(".heroi-opcao:not(:disabled)")
        .forEach((botao) => {
            botao.addEventListener("click", () => {
                const heroiId = Number(botao.dataset.heroiId);

                selecionarHeroi(heroiId);
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
        mostrarErro("O herói selecionado não foi encontrado.");
        return;
    }

    const { equipe, indice } = estado.slotAtual;
    const rota = ROTAS[indice];

    if (heroi.rota !== rota.codigo) {
        mostrarErro(
            `${heroi.nome} não está cadastrado para ${rota.nome}.`
        );
        return;
    }

    estado[equipe][indice] = heroi;

    fecharModal();
    renderizarDraft();
    limparAnaliseAnterior();
    limparErro();
}

function removerHeroiAtual() {
    if (!estado.slotAtual) {
        return;
    }

    const { equipe, indice } = estado.slotAtual;

    estado[equipe][indice] = null;

    fecharModal();
    renderizarDraft();
    limparAnaliseAnterior();
    limparErro();
}

function heroiSelecionadoEmOutroSlot(heroiId) {
    const atual = estado.slotAtual;

    return ["aliados", "inimigos"].some((equipe) =>
        estado[equipe].some((heroi, indice) => {
            if (!heroi) {
                return false;
            }

            const mesmoSlot =
                atual
                && atual.equipe === equipe
                && atual.indice === indice;

            return !mesmoSlot
                && Number(heroi.id) === Number(heroiId);
        })
    );
}

function limparDraft() {
    estado.aliados = Array(5).fill(null);
    estado.inimigos = Array(5).fill(null);

    limparErro();
    renderizarDraft();
    limparAnaliseAnterior();
}

function limparAnaliseAnterior() {
    carregamento.classList.add("oculto");
    resultadoAnalise.classList.add("oculto");
    estadoInicial.classList.remove("oculto");

    listaRecomendacoes.innerHTML = "";
    listaAvisos.innerHTML = "";
    painelAvisos.classList.add("oculto");

    rotaAnalisada.textContent = "—";
    versaoDados.textContent = "—";
    quantidadeResultados.textContent = "0 recomendações";
}

function atualizarContador() {
    const quantidade = [
        ...estado.aliados,
        ...estado.inimigos
    ].filter(Boolean).length;

    contadorDraft.textContent =
        `${quantidade} de 10 escolhas`;
}

async function analisarDraft() {
    limparErro();

    const rotaAlvo = rotaAlvoSelect.value;
    const indiceRotaAlvo = ROTAS.findIndex(
        (rota) => rota.codigo === rotaAlvo
    );

    if (indiceRotaAlvo < 0) {
        mostrarErro("Selecione uma rota válida para a recomendação.");
        return;
    }

    if (estado.aliados[indiceRotaAlvo]) {
        mostrarErro(
            "O slot aliado da rota recomendada deve permanecer vazio. "
            + "Remova o herói desse slot ou escolha outra rota."
        );
        return;
    }

    iniciarCarregamento();

    const corpo = {
        rotaAlvo,
        aliados: montarEscolhas(estado.aliados),
        inimigos: montarEscolhas(estado.inimigos)
    };

    try {
        const resposta = await fetch("/api/draft/recomendar", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(corpo)
        });

        if (!resposta.ok) {
            throw new Error(await extrairMensagemErro(resposta));
        }

        const analise = await resposta.json();

        renderizarAnalise(analise);
    } catch (erro) {
        finalizarCarregamento();
        estadoInicial.classList.remove("oculto");
        mostrarErro(erro.message);
    }
}

function montarEscolhas(equipe) {
    return equipe
        .map((heroi, indice) => {
            if (!heroi) {
                return null;
            }

            return {
                rota: ROTAS[indice].codigo,
                heroiId: Number(heroi.id)
            };
        })
        .filter(Boolean);
}

function iniciarCarregamento() {
    estado.analisando = true;
    atualizarBotaoAnalise();

    estadoInicial.classList.add("oculto");
    resultadoAnalise.classList.add("oculto");
    carregamento.classList.remove("oculto");
}

function finalizarCarregamento() {
    estado.analisando = false;
    atualizarBotaoAnalise();
    carregamento.classList.add("oculto");
}

function atualizarBotaoAnalise() {
    const bloqueado =
        estado.analisando || estado.herois.length === 0;

    botaoAnalisar.disabled = bloqueado;
    botaoAnalisar.textContent = estado.analisando
        ? "Analisando..."
        : "Analisar draft";
}

function renderizarAnalise(analise) {
    finalizarCarregamento();
    resultadoAnalise.classList.remove("oculto");

    const rota = ROTAS_POR_CODIGO[analise.rotaAlvo];
    const recomendacoes = analise.recomendacoes ?? [];
    const totalCandidatos = analise.totalCandidatos ?? 0;

    rotaAnalisada.textContent = rota?.nome ?? analise.rotaAlvo;
    versaoDados.textContent = analise.versaoDados ?? "Sem versão";

    quantidadeResultados.textContent =
        `${recomendacoes.length} recomendações de `
        + `${totalCandidatos} candidatos`;

    if (recomendacoes.length === 0) {
        listaRecomendacoes.innerHTML = `
            <div class="sem-resultados">
                Nenhum herói disponível para esta rota.
            </div>
        `;
    } else {
        listaRecomendacoes.innerHTML = recomendacoes
            .map((recomendacao, indice) =>
                criarCardRecomendacao(recomendacao, indice)
            )
            .join("");
    }

    renderizarAvisos(analise.avisos ?? []);
}

function criarCardRecomendacao(recomendacao, indice) {
    const classeNivel = normalizarClasse(recomendacao.nivel);
    const componentes = Object.entries(
        recomendacao.componentes ?? {}
    );
    const motivos = recomendacao.motivos ?? [];

    const componentesHtml = componentes
        .map(([nome, valor]) => `
            <span class="componente-pontuacao">
                <small>
                    ${escaparHtml(ROTULOS_COMPONENTES[nome] ?? nome)}
                </small>

                <strong class="${valor < 0 ? "valor-negativo" : ""}">
                    ${formatarPontuacaoComponente(nome, valor)}
                </strong>
            </span>
        `)
        .join("");

    const motivosHtml = motivos
        .map((motivo) => `
            <li>${escaparHtml(motivo)}</li>
        `)
        .join("");

    return `
        <article class="card-recomendacao">
            <div class="card-recomendacao__topo">
                <div>
                    <span class="card-recomendacao__posicao">
                        ESCOLHA ${String(indice + 1).padStart(2, "0")}
                    </span>

                    <h3>
                        ${escaparHtml(recomendacao.nome)}
                    </h3>
                </div>

                <div class="card-recomendacao__pontuacao">
                    ${recomendacao.pontuacaoFinal}
                    <small>/100</small>
                </div>
            </div>

            <div class="card-recomendacao__status">
                <span class="nivel nivel--${classeNivel}">
                    ${escaparHtml(recomendacao.nivel)}
                </span>

                <span class="validacao-dados">
                    ${
                        recomendacao.dadosValidados
                            ? "Dados revisados"
                            : "Dados provisórios"
                    }
                </span>
            </div>

            <div class="componentes-pontuacao">
                ${componentesHtml}
            </div>

            <div class="motivos-recomendacao">
                <strong>Por que esta escolha?</strong>

                <ul>
                    ${motivosHtml}
                </ul>
            </div>
        </article>
    `;
}

function renderizarAvisos(avisos) {
    if (avisos.length === 0) {
        painelAvisos.classList.add("oculto");
        listaAvisos.innerHTML = "";
        return;
    }

    listaAvisos.innerHTML = avisos
        .map((aviso) => `<li>${escaparHtml(aviso)}</li>`)
        .join("");

    painelAvisos.classList.remove("oculto");
}

async function extrairMensagemErro(resposta) {
    try {
        const erro = await resposta.json();
        const detalhes = Array.isArray(erro.detalhes)
            ? erro.detalhes.join(" ")
            : "";

        return detalhes || erro.erro || "Não foi possível analisar o draft.";
    } catch (erro) {
        return "Não foi possível analisar o draft.";
    }
}

function formatarPontuacaoComponente(nome, valor) {
    if (nome === "base") {
        return String(valor);
    }

    return valor > 0 ? `+${valor}` : String(valor);
}

function mostrarErro(mensagem) {
    mensagemErro.textContent = mensagem;
}

function limparErro() {
    mensagemErro.textContent = "";
}

function obterIniciais(nome) {
    return nome
        .split(" ")
        .slice(0, 2)
        .map((parte) => parte.charAt(0))
        .join("")
        .toUpperCase();
}

function normalizarTexto(valor) {
    return String(valor ?? "")
        .normalize("NFD")
        .replace(/\p{M}/gu, "")
        .trim()
        .toLowerCase();
}

function normalizarClasse(valor) {
    return normalizarTexto(valor)
        .replaceAll(" ", "-")
        .replaceAll("_", "-");
}

function escaparHtml(valor) {
    const elemento = document.createElement("div");

    elemento.textContent = String(valor);

    return elemento.innerHTML;
}
