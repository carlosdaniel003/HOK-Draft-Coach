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

const estado = {
    herois: [],
    aliados: Array(5).fill(null),
    inimigos: Array(5).fill(null),
    slotAtual: null
};

const slotsAliados = document.querySelector("#slots-aliados");
const slotsInimigos = document.querySelector("#slots-inimigos");

const contadorDraft = document.querySelector("#contador-draft");
const botaoLimpar = document.querySelector("#botao-limpar");
const botaoAnalisar = document.querySelector("#botao-analisar");

const mensagemErro = document.querySelector("#mensagem-erro");
const estadoInicial = document.querySelector("#estado-inicial");
const carregamento = document.querySelector("#carregamento");
const resultadoAnalise = document.querySelector("#resultado-analise");

const heroiAnalisado = document.querySelector("#heroi-analisado");
const quantidadeResultados = document.querySelector(
    "#quantidade-resultados"
);
const listaRecomendacoes = document.querySelector(
    "#lista-recomendacoes"
);

const modalHerois = document.querySelector("#modal-herois");
const modalOverlay = document.querySelector("#modal-overlay");
const botaoFecharModal = document.querySelector(
    "#botao-fechar-modal"
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

    try {
        const resposta = await fetch("/api/herois");

        if (!resposta.ok) {
            throw new Error("Não foi possível carregar os heróis.");
        }

        estado.herois = await resposta.json();
    } catch (erro) {
        mostrarErro(erro.message);
    }
}

function renderizarDraft() {
    slotsAliados.innerHTML = criarSlots("aliados");
    slotsInimigos.innerHTML = criarSlots("inimigos");

    adicionarEventosAosSlots();
    atualizarContador();
}

function criarSlots(equipe) {
    return ROTAS.map((rota, indice) => {
        const heroi = estado[equipe][indice];
        const preenchido = Boolean(heroi);

        return `
            <button
                class="
                    slot-heroi
                    ${preenchido ? "slot-heroi--preenchido" : ""}
                "
                type="button"
                data-equipe="${equipe}"
                data-indice="${indice}"
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
                                : "Selecionar herói"
                        }
                    </span>
                </span>

                <span class="slot-heroi__acao">
                    ${preenchido ? "↻" : "+"}
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
    const nomeEquipe =
        equipe === "aliados"
            ? "Equipe aliada"
            : "Equipe inimiga";

    modalTitulo.textContent = rota.nome;

    modalDescricao.textContent =
        `${nomeEquipe} · Selecione um herói para esta posição.`;

    pesquisaHeroi.value = "";

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
    const termo = normalizarTexto(filtro);

    const heroisFiltrados = [...estado.herois]
        .filter((heroi) =>
            normalizarTexto(heroi.nome).includes(termo)
        )
        .sort((heroiA, heroiB) =>
            heroiA.nome.localeCompare(heroiB.nome, "pt-BR")
        );

    if (heroisFiltrados.length === 0) {
        listaHeroisModal.innerHTML = `
            <div class="sem-resultados">
                Nenhum herói encontrado.
            </div>
        `;

        return;
    }

    listaHeroisModal.innerHTML = heroisFiltrados
        .map((heroi) => {
            const selecionado = heroiJaSelecionado(heroi.id);

            return `
                <button
                    class="heroi-opcao"
                    type="button"
                    data-heroi-id="${heroi.id}"
                    ${selecionado ? "disabled" : ""}
                >
                    <strong>
                        ${escaparHtml(heroi.nome)}
                    </strong>

                    <span>
                        ${escaparHtml(heroi.estilo)}
                    </span>
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

    estado[equipe][indice] = heroi;

    fecharModal();
    renderizarDraft();
    limparAnaliseAnterior();
}

function heroiJaSelecionado(heroiId) {
    const todosSelecionados = [
        ...estado.aliados,
        ...estado.inimigos
    ].filter(Boolean);

    return todosSelecionados.some(
        (heroi) => Number(heroi.id) === Number(heroiId)
    );
}

function limparDraft() {
    estado.aliados = Array(5).fill(null);
    estado.inimigos = Array(5).fill(null);

    mensagemErro.textContent = "";

    renderizarDraft();
    limparAnaliseAnterior();
}

function limparAnaliseAnterior() {
    carregamento.classList.add("oculto");
    resultadoAnalise.classList.add("oculto");
    estadoInicial.classList.remove("oculto");

    listaRecomendacoes.innerHTML = "";
    mensagemErro.textContent = "";
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
    mensagemErro.textContent = "";

    const indiceFarmLane = ROTAS.findIndex(
        (rota) => rota.codigo === "FARM_LANE"
    );

    const inimigoFarmLane =
        estado.inimigos[indiceFarmLane];

    if (!inimigoFarmLane) {
        mostrarErro(
            "Selecione o herói inimigo da Farm Lane antes de analisar."
        );

        return;
    }

    iniciarCarregamento();

    try {
        const endereco =
            "/api/recomendacoes/counter?inimigo=" +
            encodeURIComponent(inimigoFarmLane.nome);

        const resposta = await fetch(endereco);

        if (!resposta.ok) {
            throw new Error(
                "Não foi possível analisar o draft."
            );
        }

        const recomendacoes = await resposta.json();

        renderizarRecomendacoes(
            inimigoFarmLane,
            recomendacoes
        );
    } catch (erro) {
        carregamento.classList.add("oculto");
        estadoInicial.classList.remove("oculto");

        mostrarErro(erro.message);
    }
}

function iniciarCarregamento() {
    estadoInicial.classList.add("oculto");
    resultadoAnalise.classList.add("oculto");
    carregamento.classList.remove("oculto");
}

function renderizarRecomendacoes(
    inimigo,
    recomendacoes
) {
    carregamento.classList.add("oculto");
    resultadoAnalise.classList.remove("oculto");

    heroiAnalisado.textContent = inimigo.nome;

    quantidadeResultados.textContent =
        `${recomendacoes.length} recomendações`;

    if (recomendacoes.length === 0) {
        listaRecomendacoes.innerHTML = `
            <div class="sem-resultados">
                Ainda não existem recomendações cadastradas
                contra ${escaparHtml(inimigo.nome)}.
            </div>
        `;

        return;
    }

    listaRecomendacoes.innerHTML = recomendacoes
        .map((recomendacao, indice) =>
            criarCardRecomendacao(recomendacao, indice)
        )
        .join("");
}

function criarCardRecomendacao(
    recomendacao,
    indice
) {
    const classeNivel = recomendacao.nivel
        .toLowerCase()
        .replaceAll("_", "-");

    return `
        <article class="card-recomendacao">
            <div class="card-recomendacao__topo">
                <div>
                    <span class="card-recomendacao__posicao">
                        ESCOLHA ${String(indice + 1).padStart(2, "0")}
                    </span>

                    <h3>
                        ${escaparHtml(recomendacao.recomendado)}
                    </h3>
                </div>

                <div class="card-recomendacao__pontuacao">
                    ${recomendacao.pontuacao}
                    <small>/100</small>
                </div>
            </div>

            <span class="nivel nivel--${classeNivel}">
                ${escaparHtml(recomendacao.nivel)}
            </span>

            <p class="card-recomendacao__motivo">
                ${escaparHtml(recomendacao.motivo)}
            </p>
        </article>
    `;
}

function mostrarErro(mensagem) {
    mensagemErro.textContent = mensagem;
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

function escaparHtml(valor) {
    const elemento = document.createElement("div");

    elemento.textContent = String(valor);

    return elemento.innerHTML;
}