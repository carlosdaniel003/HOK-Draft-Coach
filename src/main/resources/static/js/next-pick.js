const painelProximoPick = document.querySelector("#painel-proximo-pick");
const conteudoProximoPick = document.querySelector("#conteudo-proximo-pick");
const statusProximoPick = document.querySelector("#status-proximo-pick");
const preferenciaFuncao = document.querySelector("#preferencia-funcao");
const modalDetalhesPick = document.querySelector("#modal-detalhes-pick");
const modalDetalhesOverlay = document.querySelector("#modal-detalhes-overlay");
const botaoFecharDetalhes = document.querySelector("#botao-fechar-detalhes");
const detalhesPickTitulo = document.querySelector("#detalhes-pick-titulo");
const conteudoDetalhesPick = document.querySelector("#conteudo-detalhes-pick");

let temporizadorProximoPick = null;
let versaoConsultaProximoPick = 0;
let resultadoAtualProximoPick = null;
let abaAnaliseAtual = "now";
let controladorConsultaProximoPick = null;
let assinaturaUltimaConsultaProximoPick = null;

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

    document
        .querySelectorAll("[data-analysis-tab]")
        .forEach((botao) => {
            botao.addEventListener("click", () => {
                abaAnaliseAtual = botao.dataset.analysisTab;
                atualizarAbasAnalise();
                renderizarAbaAnalise();
            });
        });

    modalDetalhesOverlay?.addEventListener("click", fecharDetalhesPick);
    botaoFecharDetalhes?.addEventListener("click", fecharDetalhesPick);

    document.addEventListener("keydown", (evento) => {
        if (evento.key === "Escape") {
            fecharDetalhesPick();
        }
    });

    agendarConsultaProximoPick();
}

function agendarConsultaProximoPick() {
    clearTimeout(temporizadorProximoPick);
    temporizadorProximoPick = setTimeout(
        consultarRecomendacaoProximoPick,
        90
    );
}

async function consultarRecomendacaoProximoPick() {
    const request = montarRequestProximoPick();
    const assinatura = JSON.stringify(request);

    if (
        assinatura === assinaturaUltimaConsultaProximoPick
            && resultadoAtualProximoPick
    ) {
        return;
    }

    const versaoAtual = ++versaoConsultaProximoPick;
    controladorConsultaProximoPick?.abort();
    controladorConsultaProximoPick = new AbortController();
    renderizarCarregamentoProximoPick();

    try {
        const resposta = await fetch(
            "/api/draft/recomendar-proximo-pick",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: assinatura,
                signal: controladorConsultaProximoPick.signal
            }
        );

        if (!resposta.ok) {
            throw new Error(await extrairErroProximoPick(resposta));
        }

        const resultado = await resposta.json();

        if (versaoAtual !== versaoConsultaProximoPick) {
            return;
        }

        assinaturaUltimaConsultaProximoPick = assinatura;
        resultadoAtualProximoPick = resultado;
        renderizarRecomendacaoProximoPick(resultado);
    } catch (erro) {
        if (erro.name === "AbortError") {
            return;
        }
        if (versaoAtual !== versaoConsultaProximoPick) {
            return;
        }

        resultadoAtualProximoPick = null;
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
            Recalculando necessidades, ameaças, projeções e respostas possíveis...
        </div>
    `;
}

function renderizarRecomendacaoProximoPick(resultado) {
    statusProximoPick.textContent = formatarEstadoDraft(
        resultado.estadoDraft
    );
    statusProximoPick.className =
        `coach-status coach-status--${classeTexto(resultado.estadoDraft)}`;

    atualizarAbasAnalise();
    renderizarAbaAnalise();
}

function atualizarAbasAnalise() {
    document
        .querySelectorAll("[data-analysis-tab]")
        .forEach((botao) => {
            botao.classList.toggle(
                "strategy-tab--active",
                botao.dataset.analysisTab === abaAnaliseAtual
            );
        });
}

function renderizarAbaAnalise() {
    const resultado = resultadoAtualProximoPick;

    if (!resultado) {
        return;
    }

    if (abaAnaliseAtual === "composition") {
        conteudoProximoPick.innerHTML = criarAbaComposicao(resultado);
        return;
    }

    if (abaAnaliseAtual === "enemies") {
        conteudoProximoPick.innerHTML = criarAbaInimigos(resultado);
        return;
    }

    if (abaAnaliseAtual === "projection") {
        conteudoProximoPick.innerHTML = criarAbaProjecao(resultado);
        registrarEventosDetalhes();
        return;
    }

    conteudoProximoPick.innerHTML = criarAbaAgora(resultado);
    registrarEventosDetalhes();
}

function criarAbaAgora(resultado) {
    const diagnostico = resultado.diagnosticoComposicao;
    const opcoes = obterOpcoesEstrategicas(resultado);

    if (!resultado.recomendacaoPrincipal && opcoes.length === 0) {
        return `
            <div class="coach-vazio">
                <strong>${escaparProximoPick(resultado.mensagem)}</strong>
                <p>${mensagemEstadoVazio(resultado.estadoDraft)}</p>
                ${criarAvisosProximoPick(resultado.avisos)}
            </div>
        `;
    }

    const necessidade = diagnostico?.necessidades?.[0];
    const ameacas = diagnostico?.analiseAmeacasInimigas;
    const maiorAmeaca = ameacas?.maiorAmeaca;
    const condicaoInimiga = diagnostico?.condicoesVitoriaInimigas
        ?.find((condicao) => condicao.principal)
        ?? diagnostico?.condicoesVitoriaInimigas?.[0];
    const contextoRotacao = obterContextoRotacao(resultado);
    const opcaoPrincipal = opcoes[0];

    return `
        <div class="analysis-view">
            ${criarResumoRotacao(contextoRotacao, resultado)}
            <div class="analysis-summary-grid">
                <article class="analysis-summary-card analysis-summary-card--need">
                    <span class="analysis-summary-card__eyebrow">NECESSIDADE PRINCIPAL</span>
                    <strong>${escaparProximoPick(necessidade?.titulo ?? "Aguardando mais picks aliados")}</strong>
                    <p>${escaparProximoPick(necessidade?.motivo ?? "O motor atualiza as necessidades conforme a composição é revelada.")}</p>
                </article>
                <article class="analysis-summary-card analysis-summary-card--threat">
                    <span class="analysis-summary-card__eyebrow">MAIOR AMEAÇA INIMIGA</span>
                    <strong>${escaparProximoPick(maiorAmeaca?.heroi ?? "Ainda não identificada")}</strong>
                    <p>${escaparProximoPick(maiorAmeaca?.motivos?.[0] ?? "Registre os picks inimigos para mapear ameaça, iniciador e habilitador.")}</p>
                </article>
            </div>
            ${condicaoInimiga ? `
                <article class="condition-card">
                    <span class="analysis-summary-card__eyebrow">CONDIÇÃO DE VITÓRIA INIMIGA</span>
                    <strong>${escaparProximoPick(condicaoInimiga.titulo)}</strong>
                    <p>${escaparProximoPick(condicaoInimiga.descricao)}</p>
                </article>
            ` : ""}
            <section class="recommendation-zone">
                <div class="recommendation-zone__header">
                    <span class="recommendation-zone__title">${contextoRotacao.ehMeuSlot ? "3 MELHORES OPÇÕES PARA VOCÊ" : "3 MELHORES OPÇÕES PARA O PRÓXIMO PICK ALIADO"}</span>
                    <small>${escaparProximoPick(resultado.contextoDraft?.prioridade ?? "As categorias mudam conforme a ordem do próximo pick aliado.")}</small>
                </div>
                <div class="recommendation-grid">${opcoes.map(criarCardOpcao).join("")}</div>
            </section>
            ${criarPrevisaoInimigaAgora(opcaoPrincipal, contextoRotacao)}
            ${criarAlertasAgora(resultado)}
        </div>
    `;
}

function obterOpcoesEstrategicas(resultado) {
    if (Array.isArray(resultado.opcoesEstrategicas)
        && resultado.opcoesEstrategicas.length > 0) {
        return resultado.opcoesEstrategicas;
    }

    const picks = [
        resultado.recomendacaoPrincipal,
        ...(resultado.alternativas ?? [])
    ].filter(Boolean);

    const tipos = [
        ["MELHOR_GERAL", "Melhor escolha geral"],
        ["MAIS_SEGURA", "Escolha mais segura"],
        ["MAIOR_IMPACTO", "Escolha de maior impacto"]
    ];

    return picks.slice(0, 3).map((pick, indice) => ({
        tipo: tipos[indice][0],
        titulo: tipos[indice][1],
        pontuacaoCategoria: pick.pontuacaoFinal,
        escolha: pick,
        projecao: {
            robustez: pick.piorCenario,
            piorCenarioProjetado: pick.piorCenario,
            respostasProvaveis: [],
            resumoPiorCenario: "Projeção detalhada indisponível."
        },
        explicacao: {
            resumo: resultado.mensagem,
            leituraInimiga: [],
            leituraAliada: [],
            porQueFunciona: pick.motivos ?? [],
            riscos: pick.riscos ?? [],
            planoDeJogo: "Acompanhe os próximos picks para refinar o plano."
        }
    }));
}

function criarCardOpcao(opcao, indice) {
    const tipo = opcao.tipo ?? "MELHOR_GERAL";
    const classe = tipo === "MAIS_SEGURA"
        ? "safe"
        : tipo === "MAIOR_IMPACTO"
            ? "impact"
            : "general";
    const escolha = opcao.escolha ?? {};
    const explicacao = opcao.explicacao ?? {};
    const pontos = opcao.pontuacaoCategoria
        ?? escolha.pontuacaoFinal
        ?? 0;
    const bullets = (
        explicacao.porQueFunciona?.length
            ? explicacao.porQueFunciona
            : escolha.motivos
    ) ?? [];

    return `
        <article class="pick-option-card pick-option-card--${classe}">
            <div class="pick-option-card__category">
                <span class="pick-option-card__rank">${indice + 1}</span>
                <span>${escaparProximoPick(opcao.titulo ?? formatarTipoOpcao(tipo))}</span>
            </div>
            <div class="pick-option-card__avatar">${iniciaisHeroi(escolha.heroi)}</div>
            <h3>${escaparProximoPick(escolha.heroi ?? "—")}</h3>
            <span class="pick-option-card__score-label">${rotuloPontuacao(tipo)}</span>
            <div class="pick-option-card__score">
                <strong>${pontos}</strong><span>/100</span>
            </div>
            <ul>
                ${bullets.slice(0, 4).map((item) => `
                    <li>${escaparProximoPick(item)}</li>
                `).join("")}
            </ul>
            <button
                class="pick-option-card__button"
                type="button"
                data-option-details="${indice}"
            >Ver análise completa</button>
        </article>
    `;
}

function criarAlertasAgora(resultado) {
    const diagnostico = resultado.diagnosticoComposicao;
    const alertas = [];

    diagnostico?.diagnosticosTemporais
        ?.filter((item) => item.severidade !== "INFO")
        .slice(0, 2)
        .forEach((item) => alertas.push(item.titulo));

    diagnostico?.penalidades
        ?.slice(0, 2)
        .forEach((item) => alertas.push(item.titulo));

    diagnostico?.antiSinergiasNossaComposicao
        ?.slice(0, 1)
        .forEach((item) => alertas.push(item.descricao));

    if (alertas.length === 0) {
        return "";
    }

    return `
        <section class="analysis-alerts">
            <strong>⚠ ALERTAS IMPORTANTES</strong>
            <ul>
                ${alertas.map((alerta) => `
                    <li>${escaparProximoPick(alerta)}</li>
                `).join("")}
            </ul>
        </section>
    `;
}

function obterContextoRotacao(resultado) {
    const lado = document.querySelector("#meu-lado")?.value;
    const ordemUsuario = Number(document.querySelector("#minha-ordem")?.value);
    const slotAliado = encontrarProximoSlotAliado(lado);
    const prefixoUsuario = LADOS[lado]?.prefixo;
    const slotUsuario = prefixoUsuario && ordemUsuario ? `${prefixoUsuario}${ordemUsuario}` : null;
    const ladoAtual = resultado.proximoLado === "AZUL" ? "Equipe Azul" : resultado.proximoLado === "VERMELHO" ? "Equipe Vermelha" : "A definir";
    return {
        ladoAtual,
        slotsAtuais: resultado.proximosSlots ?? [],
        slotAliado,
        ehMeuSlot: Boolean(slotAliado && slotAliado === slotUsuario),
        inimigoEscolhendo: resultado.estadoDraft === "AGUARDANDO_INIMIGO"
    };
}

function encontrarProximoSlotAliado(lado) {
    if (!LADOS[lado] || !estado.picks[lado]) return null;
    for (const rodada of SEQUENCIA_PICKS) {
        if (rodada.lado !== lado) continue;
        for (const indice of rodada.indices) {
            if (!estado.picks[lado][indice]) return `${LADOS[lado].prefixo}${indice + 1}`;
        }
    }
    return null;
}

function criarResumoRotacao(contexto, resultado) {
    const acao = resultado.estadoDraft === "MINHA_VEZ" ? "Seu pick está aberto" : resultado.estadoDraft === "VEZ_ALIADA" ? "Sua equipe está escolhendo" : resultado.estadoDraft === "AGUARDANDO_INIMIGO" ? "A equipe inimiga está escolhendo" : formatarEstadoDraft(resultado.estadoDraft);
    const slots = contexto.slotsAtuais.length ? contexto.slotsAtuais.join(" · ") : "Nenhum slot aberto";
    return `
        <section class="rotation-overview">
            <article class="rotation-overview__item"><span>ROTAÇÃO ATUAL</span><strong>${escaparProximoPick(acao)}</strong><p>${escaparProximoPick(contexto.ladoAtual)} · ${escaparProximoPick(slots)}</p></article>
            <article class="rotation-overview__item rotation-overview__item--ally"><span>RECOMENDAÇÃO ALIADA</span><strong>${escaparProximoPick(contexto.ehMeuSlot ? `Seu slot ${contexto.slotAliado}` : `Próximo aliado ${contexto.slotAliado ?? "—"}`)}</strong><p>${escaparProximoPick(resultado.mensagem)}</p></article>
            <article class="rotation-overview__item rotation-overview__item--enemy"><span>LEITURA FUTURA</span><strong>Respostas inimigas calculadas</strong><p>O sistema simula counters, rotas abertas e o pior cenário após cada opção aliada.</p></article>
        </section>
    `;
}

function criarPrevisaoInimigaAgora(opcao, contexto) {
    const projecao = opcao?.projecao ?? {};
    const respostas = projecao.respostasProvaveis ?? [];
    return `
        <section class="enemy-forecast">
            <div class="enemy-forecast__header"><div><span class="analysis-section__title">PICKS INIMIGOS PROVÁVEIS</span><strong>Após ${escaparProximoPick(opcao?.escolha?.heroi ?? "o pick aliado recomendado")}</strong></div><small>${contexto.inimigoEscolhendo ? "INIMIGO ESCOLHENDO AGORA" : "PRÓXIMA RESPOSTA INIMIGA"}</small></div>
            <div class="enemy-forecast__grid">
                ${respostas.slice(0, 3).map((resposta, indice) => `
                    <article class="enemy-forecast-card"><span class="enemy-forecast-card__rank">${indice + 1}</span><div><strong>${escaparProximoPick(resposta.heroi)}</strong><small>${nomeRotaProximoPick(resposta.rota)} · ${resposta.probabilidadeHeuristica}% provável</small><p>${escaparProximoPick(resposta.motivos?.[0] ?? `Impacto estimado de ${resposta.impactoContraNossaComposicao}/100 contra nossa composição.`)}</p></div></article>
                `).join("") || criarLinhaVazia("Ainda não há informação suficiente para prever um herói inimigo específico.")}
            </div>
        </section>
    `;
}

function criarAbaComposicao(resultado) {
    const diagnostico = resultado.diagnosticoComposicao;

    if (!diagnostico) {
        return criarEstadoSemDiagnostico(
            "A composição será analisada depois do primeiro pick aliado."
        );
    }

    const vetor = diagnostico.nossaComposicao?.vetor ?? {};
    const dimensoes = Object.entries(vetor)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 9);
    const curva = diagnostico.curvaPoderNossaComposicao;
    const economia = diagnostico.economiaNossaComposicao;
    const condicao = diagnostico.nossasCondicoesVitoria
        ?.find((item) => item.principal)
        ?? diagnostico.nossasCondicoesVitoria?.[0];

    return `
        <div class="analysis-view">
            ${condicao ? `
                <article class="condition-card">
                    <span class="analysis-summary-card__eyebrow">NOSSA CONDIÇÃO DE VITÓRIA</span>
                    <strong>${escaparProximoPick(condicao.titulo)}</strong>
                    <p>${escaparProximoPick(condicao.descricao)}</p>
                </article>
            ` : ""}

            <section class="analysis-section">
                <span class="analysis-section__title">DNA DA COMPOSIÇÃO</span>
                <div class="analysis-metrics">
                    ${dimensoes.map(([dimensao, valor]) => `
                        <div class="analysis-metric">
                            <span class="metric-label">${nomeDimensao(dimensao)}</span>
                            <strong>${valor}/100</strong>
                            <div class="metric-bar"><span style="width:${valor}%"></span></div>
                        </div>
                    `).join("")}
                </div>
            </section>

            <section class="analysis-section">
                <span class="analysis-section__title">NECESSIDADES IMEDIATAS</span>
                <div class="need-list">
                    ${(diagnostico.necessidades ?? []).slice(0, 5).map((item) => `
                        <article class="need-item">
                            <div class="need-item__top">
                                <strong>${escaparProximoPick(item.titulo)}</strong>
                                <small>Urgência ${item.urgencia}/100</small>
                            </div>
                            <p>${escaparProximoPick(item.motivo)}</p>
                        </article>
                    `).join("") || criarLinhaVazia("Nenhuma necessidade crítica detectada.")}
                </div>
            </section>

            <section class="analysis-section">
                <span class="analysis-section__title">CURVA DE PODER E ECONOMIA</span>
                <div class="analysis-metrics">
                    ${criarMetricaSimples("Early game", curva?.earlyGame ?? 0)}
                    ${criarMetricaSimples("Mid game", curva?.midGame ?? 0)}
                    ${criarMetricaSimples("Late game", curva?.lateGame ?? 0)}
                    ${criarMetricaSimples("Carga econômica", economia?.cargaEconomica ?? 0)}
                    ${criarMetricaSimples("Conflito de recursos", economia?.conflitoDeRecursos ?? 0)}
                    ${criarMetricaTexto("Pico", formatarEnum(curva?.pico ?? "—"))}
                </div>
            </section>

            <section class="analysis-section">
                <span class="analysis-section__title">SINERGIAS E RISCOS</span>
                <div class="synergy-list">
                    ${(diagnostico.sinergiasGrupoNossaComposicao ?? [])
                        .filter((item) => item.ativa)
                        .slice(0, 4)
                        .map((item) => criarItemTexto(item.codigo, item.descricao))
                        .join("")}
                    ${(diagnostico.penalidades ?? [])
                        .slice(0, 3)
                        .map((item) => criarItemTexto(item.titulo, item.motivo, "penalty-item"))
                        .join("")}
                    ${!(diagnostico.sinergiasGrupoNossaComposicao ?? []).some((item) => item.ativa)
                        && !(diagnostico.penalidades ?? []).length
                        ? criarLinhaVazia("Nenhuma sinergia de grupo ou penalidade relevante ainda.")
                        : ""}
                </div>
            </section>
        </div>
    `;
}

function criarAbaInimigos(resultado) {
    const diagnostico = resultado.diagnosticoComposicao;
    const ameacas = diagnostico?.analiseAmeacasInimigas;

    if (!ameacas?.maiorAmeaca) {
        return criarEstadoSemDiagnostico(
            "Registre os picks inimigos para identificar ameaça, protetor, iniciador, habilitador e elo fraco."
        );
    }

    const papeis = [
        ["Maior ameaça", ameacas.maiorAmeaca, "main"],
        ["Protetor", ameacas.protetorPrincipal],
        ["Iniciador", ameacas.iniciadorPrincipal],
        ["Habilitador", ameacas.habilitadorCritico],
        ["Elo fraco", ameacas.eloFraco]
    ].filter(([, perfil]) => perfil);

    return `
        <div class="analysis-view">
            <section class="threat-grid">
                ${papeis.map(([papel, perfil, destaque]) => `
                    <article class="threat-card ${destaque ? "threat-card--main" : ""}">
                        <span class="threat-card__role">${papel}</span>
                        <strong>${escaparProximoPick(perfil.heroi)}</strong>
                        <p>${escaparProximoPick(
                            perfil.motivos?.[0]
                            ?? criarResumoPerfilAmeaca(perfil)
                        )}</p>
                    </article>
                `).join("")}
            </section>

            <article class="condition-card">
                <span class="analysis-summary-card__eyebrow">PLANO DE RESPOSTA</span>
                <strong>Como desmontar a composição inimiga</strong>
                <p>${escaparProximoPick(ameacas.planoResposta)}</p>
            </article>

            <section class="analysis-section">
                <span class="analysis-section__title">ALVOS PRIORITÁRIOS</span>
                <div class="threat-list">
                    ${(ameacas.alvosPrioritarios ?? []).map((alvo) => `
                        <article class="target-item">
                            <div class="target-item__top">
                                <strong>${escaparProximoPick(alvo.heroi)} · ${formatarEnum(alvo.papel)}</strong>
                                <small>Prioridade ${alvo.prioridade}/100</small>
                            </div>
                            <p>${escaparProximoPick(alvo.justificativa)}</p>
                        </article>
                    `).join("")}
                </div>
            </section>

            <section class="analysis-section">
                <span class="analysis-section__title">CONDIÇÕES DE VITÓRIA INIMIGAS</span>
                <div class="synergy-list">
                    ${(diagnostico.condicoesVitoriaInimigas ?? []).slice(0, 3).map((item) =>
                        criarItemTexto(item.titulo, item.descricao)
                    ).join("") || criarLinhaVazia("Condição ainda não identificada.")}
                </div>
            </section>
        </div>
    `;
}

function criarAbaProjecao(resultado) {
    const opcoes = obterOpcoesEstrategicas(resultado);
    if (opcoes.length === 0) return criarEstadoSemDiagnostico("As projeções aparecerão quando houver candidatos disponíveis para o próximo pick aliado.");
    const contexto = obterContextoRotacao(resultado);
    const principal = opcoes[0];
    const respostasPrincipais = principal?.projecao?.respostasProvaveis ?? [];
    return `
        <div class="analysis-view">
            <section class="projection-flow">
                <article class="projection-flow__step projection-flow__step--ally"><span>1 · PICK ALIADO</span><strong>${escaparProximoPick(principal?.escolha?.heroi ?? "—")}</strong><p>${escaparProximoPick(contexto.slotAliado ?? "Próximo slot aliado")}</p></article>
                <article class="projection-flow__step projection-flow__step--enemy"><span>2 · RESPOSTAS INIMIGAS</span><strong>${respostasPrincipais.length} cenários principais</strong><p>${respostasPrincipais.slice(0, 2).map((item) => item.heroi).join(" · ") || "Aguardando informação"}</p></article>
                <article class="projection-flow__step projection-flow__step--risk"><span>3 · PIOR CENÁRIO</span><strong>${principal?.projecao?.piorCenarioProjetado ?? 0}/100</strong><p>Robustez ${principal?.projecao?.robustez ?? 0}/100</p></article>
            </section>
            <article class="condition-card"><span class="analysis-summary-card__eyebrow">SIMULAÇÃO DOS PRÓXIMOS PICKS</span><strong>Aliado recomendado, respostas inimigas e pior cenário</strong><p>Para cada opção aliada, o motor projeta os heróis inimigos mais prováveis pelas rotas abertas, capacidade de counter, condição de vitória e impacto contra nosso DNA.</p></article>
            <section>${opcoes.map((opcao, indice) => criarBlocoProjecao(opcao, indice)).join("")}</section>
        </div>
    `;
}

function criarBlocoProjecao(opcao, indice) {
    const projecao = opcao.projecao ?? {};
    const respostas = projecao.respostasProvaveis ?? [];
    return `
        <article class="projection-option">
            <div class="projection-option__header"><div><span class="analysis-summary-card__eyebrow">${escaparProximoPick(opcao.titulo)}</span><h3>${escaparProximoPick(opcao.escolha?.heroi ?? "—")}</h3><small class="projection-option__slot">Próximo pick aliado</small></div><div class="projection-score"><strong>${projecao.robustez ?? 0}/100</strong><small>ROBUSTEZ</small></div></div>
            <div class="projection-list">
                ${respostas.map((resposta, respostaIndice) => `
                    <article class="projection-answer"><div class="projection-answer__top"><strong>${respostaIndice + 1}. ${escaparProximoPick(resposta.heroi)} · ${nomeRotaProximoPick(resposta.rota)}</strong><small>${resposta.probabilidadeHeuristica}% provável</small></div><p>Impacto contra nossa composição: ${resposta.impactoContraNossaComposicao}/100 · Pontuação inimiga: ${resposta.pontuacaoInimiga}/100.</p>${(resposta.motivos ?? []).length ? `<ul class="projection-answer__reasons">${resposta.motivos.slice(0, 2).map((motivo) => `<li>${escaparProximoPick(motivo)}</li>`).join("")}</ul>` : ""}</article>
                `).join("") || criarLinhaVazia("Nenhuma resposta futura específica foi identificada neste estado.")}
            </div>
            <div class="projection-option__worst-case"><strong>Pior cenário projetado: ${projecao.piorCenarioProjetado ?? 0}/100</strong><p>${escaparProximoPick(projecao.resumoPiorCenario ?? "")}</p></div>
            <button class="pick-option-card__button" type="button" data-option-details="${indice}" style="margin-top:10px">Ver análise completa</button>
        </article>
    `;
}

function registrarEventosDetalhes() {
    document
        .querySelectorAll("[data-option-details]")
        .forEach((botao) => {
            botao.addEventListener("click", () => {
                abrirDetalhesPick(Number(botao.dataset.optionDetails));
            });
        });
}

function abrirDetalhesPick(indice) {
    const opcoes = obterOpcoesEstrategicas(resultadoAtualProximoPick ?? {});
    const opcao = opcoes[indice];

    if (!opcao || !modalDetalhesPick) {
        return;
    }

    const escolha = opcao.escolha ?? {};
    const explicacao = opcao.explicacao ?? {};
    const projecao = opcao.projecao ?? {};

    detalhesPickTitulo.textContent = `${opcao.titulo} · ${escolha.heroi}`;
    conteudoDetalhesPick.innerHTML = `
        <div class="pick-details-hero">
            <div class="pick-details-avatar">${iniciaisHeroi(escolha.heroi)}</div>
            <div>
                <h3>${escaparProximoPick(escolha.heroi)}</h3>
                <p>${escaparProximoPick(explicacao.resumo ?? resultadoAtualProximoPick.mensagem)}</p>
            </div>
        </div>

        <div class="pick-details-grid">
            ${criarBlocoDetalhe("Leitura inimiga", explicacao.leituraInimiga)}
            ${criarBlocoDetalhe("Leitura aliada", explicacao.leituraAliada)}
            ${criarBlocoDetalhe("Por que funciona", explicacao.porQueFunciona)}
            ${criarBlocoDetalhe("Riscos", explicacao.riscos)}
            <section class="pick-details-block pick-details-block--wide">
                <h4>Plano de jogo</h4>
                <p>${escaparProximoPick(explicacao.planoDeJogo ?? "Plano ainda não disponível.")}</p>
            </section>
            <section class="pick-details-block pick-details-block--wide">
                <h4>Pior cenário projetado</h4>
                <p>${escaparProximoPick(projecao.resumoPiorCenario ?? "Sem resposta futura projetada.")}</p>
            </section>
        </div>
    `;

    modalDetalhesPick.classList.remove("oculto");
    modalDetalhesPick.setAttribute("aria-hidden", "false");
}

function fecharDetalhesPick() {
    modalDetalhesPick?.classList.add("oculto");
    modalDetalhesPick?.setAttribute("aria-hidden", "true");
}

function criarBlocoDetalhe(titulo, itens = []) {
    return `
        <section class="pick-details-block">
            <h4>${escaparProximoPick(titulo)}</h4>
            ${itens?.length ? `
                <ul>
                    ${itens.map((item) => `<li>${escaparProximoPick(item)}</li>`).join("")}
                </ul>
            ` : "<p>Nenhuma observação adicional.</p>"}
        </section>
    `;
}

function criarMetricaSimples(titulo, valor) {
    return `
        <div class="analysis-metric">
            <span class="metric-label">${escaparProximoPick(titulo)}</span>
            <strong>${valor}/100</strong>
            <div class="metric-bar"><span style="width:${Math.max(0, Math.min(100, valor))}%"></span></div>
        </div>
    `;
}

function criarMetricaTexto(titulo, valor) {
    return `
        <div class="analysis-metric">
            <span class="metric-label">${escaparProximoPick(titulo)}</span>
            <strong>${escaparProximoPick(valor)}</strong>
        </div>
    `;
}

function criarItemTexto(titulo, texto, classe = "synergy-item") {
    return `
        <article class="${classe}">
            <strong>${escaparProximoPick(formatarEnum(titulo))}</strong>
            <p>${escaparProximoPick(texto ?? "")}</p>
        </article>
    `;
}

function criarLinhaVazia(texto) {
    return `
        <article class="need-item">
            <p>${escaparProximoPick(texto)}</p>
        </article>
    `;
}

function criarEstadoSemDiagnostico(texto) {
    return `
        <div class="coach-vazio">
            <strong>Análise em formação</strong>
            <p>${escaparProximoPick(texto)}</p>
        </div>
    `;
}

function criarAvisosProximoPick(avisos = []) {
    if (avisos.length === 0) {
        return "";
    }

    return `
        <ul class="coach-avisos">
            ${avisos.map((aviso) => `
                <li>${escaparProximoPick(aviso)}</li>
            `).join("")}
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

function mensagemEstadoVazio(estadoDraft) {
    const mensagens = {
        FASE_DE_BANS: "Complete os três bans de cada equipe. O motor continuará acompanhando o estado, mas as recomendações serão liberadas nos picks.",
        AGUARDANDO_IDENTIFICACAO: "Informe seu lado, sua posição e sua função no topo da tela.",
        PICK_JA_REALIZADO: "Seu pick já foi registrado. O motor continuará recomendando os próximos picks aliados.",
        EQUIPE_ALIADA_COMPLETA: "Os picks aliados terminaram. Continue registrando o inimigo para concluir a leitura.",
        DRAFT_CONCLUIDO: "O draft terminou. Consulte as abas de composição, inimigos e projeção para revisar o plano de jogo.",
        COMPOSICAO_ALIADA_INCOMPATIVEL: "A distribuição atual repete funções e precisa ser corrigida.",
        SEM_CANDIDATOS: "Nenhum candidato válido foi encontrado para as funções preferidas."
    };

    return mensagens[estadoDraft]
        ?? "Continue registrando bans e picks para formar a análise.";
}

function criarResumoPerfilAmeaca(perfil) {
    return `Potencial de vitória ${perfil.potencialVitoria ?? 0}/100, iniciação ${perfil.iniciacao ?? 0}/100 e vulnerabilidade ${perfil.vulnerabilidade ?? 0}/100.`;
}

function iniciaisHeroi(nome) {
    return String(nome ?? "?")
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((parte) => parte.charAt(0))
        .join("")
        .toUpperCase();
}

function rotuloPontuacao(tipo) {
    if (tipo === "MAIS_SEGURA") {
        return "SEGURANÇA";
    }
    if (tipo === "MAIOR_IMPACTO") {
        return "IMPACTO";
    }
    return "PONTUAÇÃO GERAL";
}

function formatarTipoOpcao(tipo) {
    const nomes = {
        MELHOR_GERAL: "Melhor escolha geral",
        MAIS_SEGURA: "Escolha mais segura",
        MAIOR_IMPACTO: "Escolha de maior impacto"
    };
    return nomes[tipo] ?? formatarEnum(tipo);
}

function nomeDimensao(dimensao) {
    const nomes = {
        ENGAGE: "Iniciação",
        DESENGAGE: "Desengage",
        PEEL: "Peel",
        POKE: "Poke",
        EXPLOSAO: "Explosão",
        DPS: "DPS",
        LINHA_DE_FRENTE: "Linha de frente",
        SUSTAIN: "Sustain",
        WAVE_CLEAR: "Wave clear",
        OBJETIVOS: "Objetivos",
        CONTROLE: "Controle",
        MOBILIDADE: "Mobilidade",
        ALCANCE: "Alcance",
        ANTI_TANQUE: "Anti-tanque",
        ANTI_CURA: "Anti-cura",
        PRESSAO_LATERAL: "Pressão lateral",
        DIVE: "Dive",
        PROTECAO: "Proteção",
        ESCALAMENTO: "Escalamento"
    };
    return nomes[dimensao] ?? formatarEnum(dimensao);
}

function formatarEstadoDraft(estadoDraft) {
    const estados = {
        MINHA_VEZ: "Sua vez",
        VEZ_ALIADA: "Vez aliada",
        AGUARDANDO_INIMIGO: "Inimigo escolhendo",
        PLANEJAMENTO: "Planejamento",
        FASE_DE_BANS: "Fase de bans",
        AGUARDANDO_IDENTIFICACAO: "Configuração pendente",
        PICK_JA_REALIZADO: "Pick registrado",
        DRAFT_CONCLUIDO: "Draft concluído",
        EQUIPE_ALIADA_COMPLETA: "Equipe aliada completa",
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

    return nomes[rota] ?? formatarEnum(rota);
}

function formatarEnum(valor) {
    return String(valor ?? "")
        .toLowerCase()
        .split("_")
        .filter(Boolean)
        .map((parte) => parte.charAt(0).toUpperCase() + parte.slice(1))
        .join(" ");
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
