const painelProximoPick = document.querySelector("#painel-proximo-pick");
const conteudoProximoPick = document.querySelector("#conteudo-proximo-pick");
const statusProximoPick = document.querySelector("#status-proximo-pick");
const preferenciaFuncao = document.querySelector("#preferencia-funcao");

let temporizadorProximoPick = null;
let versaoConsultaProximoPick = 0;

document.addEventListener("DOMContentLoaded", iniciarMotorProximoPick);

function iniciarMotorProximoPick() {
    const alvos = [
        "#bans-azul",
        "#bans-vermelho",
        "#picks-azul",
        "#picks-vermelho"
    ];

    const observador = new MutationObserver(agendarConsultaProximoPick);

    alvos.forEach((seletor) => {
        const elemento = document.querySelector(seletor);
        if (elemento) {
            observador.observe(elemento, {
                childList: true,
                subtree: true
            });
        }
    });

    document
        .querySelector("#meu-lado")
        ?.addEventListener("change", agendarConsultaProximoPick);

    document
        .querySelector("#minha-ordem")
        ?.addEventListener("change", agendarConsultaProximoPick);

    preferenciaFuncao?.addEventListener(
        "change",
        agendarConsultaProximoPick
    );

    agendarConsultaProximoPick();
}

function agendarConsultaProximoPick() {
    clearTimeout(temporizadorProximoPick);
    temporizadorProximoPick = setTimeout(
        consultarRecomendacaoProximoPick,
        180
    );
}

async function consultarRecomendacaoProximoPick() {
    const versaoAtual = ++versaoConsultaProximoPick;
    renderizarCarregamentoProximoPick();

    try {
        const resposta = await fetch(
            "/api/draft/recomendar-proximo-pick",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(montarRequestProximoPick())
            }
        );

        if (!resposta.ok) {
            throw new Error(await extrairErroProximoPick(resposta));
        }

        const resultado = await resposta.json();

        if (versaoAtual !== versaoConsultaProximoPick) {
            return;
        }

        renderizarRecomendacaoProximoPick(resultado);
    } catch (erro) {
        if (versaoAtual !== versaoConsultaProximoPick) {
            return;
        }

        renderizarErroProximoPick(erro.message);
    }
}

function montarRequestProximoPick() {
    const lado = document.querySelector("#meu-lado")?.value;
    const ordem = document.querySelector("#minha-ordem")?.value;
    const funcao = preferenciaFuncao?.value;

    return {
        meuLado: lado && lado !== "INDEFINIDO" ? lado : null,
        minhaOrdem: ordem ? Number(ordem) : null,
        bansAzul: idsHerois(estado.bans.AZUL),
        bansVermelho: idsHerois(estado.bans.VERMELHO),
        picksAzul: picksComOrdem(estado.picks.AZUL),
        picksVermelho: picksComOrdem(estado.picks.VERMELHO),
        funcoesPreferidas: funcao ? [funcao] : []
    };
}

function idsHerois(herois) {
    return herois
        .filter(Boolean)
        .map((heroi) => Number(heroi.id));
}

function picksComOrdem(herois) {
    return herois
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

function renderizarCarregamentoProximoPick() {
    statusProximoPick.textContent = "Calculando";
    statusProximoPick.className = "coach-status coach-status--carregando";

    conteudoProximoPick.innerHTML = `
        <div class="coach-carregando">
            <span class="coach-carregando__circulo"></span>
            Avaliando todos os heróis disponíveis nas hipóteses atuais...
        </div>
    `;
}

function renderizarRecomendacaoProximoPick(resultado) {
    statusProximoPick.textContent = formatarEstadoDraft(
        resultado.estadoDraft
    );
    statusProximoPick.className =
        `coach-status coach-status--${classeTexto(resultado.estadoDraft)}`;

    if (!resultado.recomendacaoPrincipal) {
        conteudoProximoPick.innerHTML = `
            <div class="coach-vazio">
                <strong>${escaparProximoPick(resultado.mensagem)}</strong>
                ${criarAvisosProximoPick(resultado.avisos)}
            </div>
        `;
        return;
    }

    const principal = resultado.recomendacaoPrincipal;

    conteudoProximoPick.innerHTML = `
        <div class="coach-resposta">
            <section class="coach-principal">
                <div class="coach-principal__topo">
                    <div>
                        <span class="coach-principal__etiqueta">
                            ${
                                resultado.ehMinhaVez
                                    ? "Pegue agora"
                                    : "Planejamento"
                            }
                        </span>

                        <h3>${escaparProximoPick(principal.heroi)}</h3>

                        <div class="coach-rotas">
                            ${principal.rotasRecomendadas
                                .map((rota) => `
                                    <span>${nomeRotaProximoPick(rota)}</span>
                                `)
                                .join("")}
                        </div>
                    </div>

                    <div class="coach-nota">
                        <strong>${principal.pontuacaoFinal}</strong>
                        <span>/100</span>
                    </div>
                </div>

                <div class="coach-metricas">
                    <span>
                        <small>Segurança</small>
                        <strong>${escaparProximoPick(principal.seguranca)}</strong>
                    </span>
                    <span>
                        <small>Média</small>
                        <strong>${principal.mediaCenarios}</strong>
                    </span>
                    <span>
                        <small>Pior cenário</small>
                        <strong>${principal.piorCenario}</strong>
                    </span>
                    <span>
                        <small>Cobertura</small>
                        <strong>${principal.coberturaHipoteses}%</strong>
                    </span>
                </div>

                <p class="coach-mensagem">
                    ${escaparProximoPick(resultado.mensagem)}
                </p>

                <div class="coach-explicacao">
                    <div>
                        <strong>Por que pegar</strong>
                        <ul>
                            ${principal.motivos
                                .map((motivo) => `
                                    <li>${escaparProximoPick(motivo)}</li>
                                `)
                                .join("")}
                        </ul>
                    </div>

                    <div>
                        <strong>Riscos</strong>
                        <ul>
                            ${principal.riscos
                                .map((risco) => `
                                    <li>${escaparProximoPick(risco)}</li>
                                `)
                                .join("")}
                        </ul>
                    </div>
                </div>
            </section>

            <aside class="coach-alternativas">
                <div class="coach-alternativas__cabecalho">
                    <span>Alternativas</span>
                    <small>
                        ${resultado.hipotesesAliadas} ×
                        ${resultado.hipotesesInimigas} hipóteses
                    </small>
                </div>

                ${criarAlternativasProximoPick(resultado.alternativas)}
                ${criarAvisosProximoPick(resultado.avisos)}
            </aside>
        </div>
    `;
}

function criarAlternativasProximoPick(alternativas) {
    if (!alternativas || alternativas.length === 0) {
        return `
            <p class="coach-sem-alternativas">
                Nenhuma alternativa adicional disponível.
            </p>
        `;
    }

    return alternativas
        .map((alternativa, indice) => `
            <article class="coach-alternativa">
                <span class="coach-alternativa__ordem">
                    ${indice + 2}
                </span>

                <div>
                    <strong>${escaparProximoPick(alternativa.heroi)}</strong>
                    <small>
                        ${alternativa.rotasRecomendadas
                            .map(nomeRotaProximoPick)
                            .join(" / ")}
                    </small>
                </div>

                <span class="coach-alternativa__nota">
                    ${alternativa.pontuacaoFinal}
                </span>
            </article>
        `)
        .join("");
}

function criarAvisosProximoPick(avisos = []) {
    if (avisos.length === 0) {
        return "";
    }

    return `
        <ul class="coach-avisos">
            ${avisos
                .map((aviso) => `
                    <li>${escaparProximoPick(aviso)}</li>
                `)
                .join("")}
        </ul>
    `;
}

function renderizarErroProximoPick(mensagem) {
    statusProximoPick.textContent = "Erro";
    statusProximoPick.className = "coach-status coach-status--erro";

    conteudoProximoPick.innerHTML = `
        <div class="coach-vazio coach-vazio--erro">
            <strong>Não foi possível calcular a recomendação.</strong>
            <p>${escaparProximoPick(mensagem)}</p>
        </div>
    `;
}

async function extrairErroProximoPick(resposta) {
    try {
        const erro = await resposta.json();

        if (Array.isArray(erro.detalhes) && erro.detalhes.length > 0) {
            return erro.detalhes.join(" ");
        }

        return erro.erro || "Erro ao calcular a recomendação.";
    } catch (erro) {
        return "Erro ao calcular a recomendação.";
    }
}

function formatarEstadoDraft(estadoDraft) {
    const estados = {
        MINHA_VEZ: "Sua vez",
        PLANEJAMENTO: "Planejamento",
        FASE_DE_BANS: "Fase de bans",
        AGUARDANDO_IDENTIFICACAO: "Configuração pendente",
        PICK_JA_REALIZADO: "Pick registrado",
        DRAFT_CONCLUIDO: "Draft concluído",
        COMPOSICAO_ALIADA_INCOMPATIVEL: "Conflito de funções",
        SEM_CANDIDATOS: "Sem candidatos"
    };

    return estados[estadoDraft] ?? estadoDraft;
}

function nomeRotaProximoPick(rota) {
    const nomes = {
        CLASH_LANE: "Clash",
        JUNGLE: "Jungle",
        MID_LANE: "Mid",
        FARM_LANE: "Farm",
        ROAMING: "Roaming"
    };

    return nomes[rota] ?? rota;
}

function classeTexto(valor) {
    return String(valor ?? "")
        .normalize("NFD")
        .replace(/\p{M}/gu, "")
        .toLowerCase()
        .replaceAll("_", "-")
        .replaceAll(" ", "-");
}

function escaparProximoPick(valor) {
    const elemento = document.createElement("div");
    elemento.textContent = String(valor ?? "");
    return elemento.innerHTML;
}
