const FUNCOES_SLOTS_ALIADOS = Object.freeze([
    Object.freeze({ codigo: "CLASH_LANE", nome: "Top Lane" }),
    Object.freeze({ codigo: "JUNGLE", nome: "Jungle" }),
    Object.freeze({ codigo: "MID_LANE", nome: "Mid Lane" }),
    Object.freeze({ codigo: "FARM_LANE", nome: "ADC" }),
    Object.freeze({ codigo: "ROAMING", nome: "Suporte" })
]);

Object.assign(NOMES_ROTAS, {
    CLASH_LANE: "Top Lane",
    JUNGLE: "Jungle",
    MID_LANE: "Mid Lane",
    FARM_LANE: "ADC",
    ROAMING: "Suporte"
});

estado.funcoesAliadas = estado.funcoesAliadas ?? {
    AZUL: Array(5).fill(""),
    VERMELHO: Array(5).fill("")
};

const preferenciaFuncaoEquipe = document.querySelector("#preferencia-funcao");
const criarSlotsPickBaseFuncoes = criarSlotsPick;
const registrarEventosSlotsBaseFuncoes = registrarEventosSlots;
const abrirModalBaseFuncoes = abrirModal;
const selecionarHeroiBaseFuncoes = selecionarHeroi;
const montarRequestProximoPickBaseFuncoes = montarRequestProximoPick;
const formatarEnumBaseFuncoes = formatarEnum;

criarSlotsPick = function criarSlotsPickComFuncoes(lado, leitura) {
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
            const exibirFuncaoAliada =
                meuLado === lado
                && Boolean(minhaOrdem)
                && minhaOrdem !== indice + 1;
            const funcaoSelecionada =
                estado.funcoesAliadas[lado]?.[indice] ?? "";

            return `
                <div class="slot-pick-wrapper ${exibirFuncaoAliada ? "slot-pick-wrapper--com-funcao" : ""}">
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

                            ${preenchido ? criarChipsRotas(heroi) : ""}
                        </span>

                        <span class="slot-pick__acao">
                            ${preenchido ? "↻" : "+"}
                        </span>
                    </button>
                    ${
                        exibirFuncaoAliada
                            ? criarSeletorFuncaoAliada(
                                lado,
                                indice,
                                funcaoSelecionada
                            )
                            : ""
                    }
                </div>
            `;
        })
        .join("");
};

registrarEventosSlots = function registrarEventosSlotsComFuncoes() {
    registrarEventosSlotsBaseFuncoes();
    document
        .querySelectorAll("[data-funcao-aliada]")
        .forEach((select) => {
            select.addEventListener("change", () => {
                atualizarFuncaoAliada(
                    select.dataset.lado,
                    Number(select.dataset.indice),
                    select.value
                );
            });
        });
};

abrirModal = function abrirModalComFuncaoFixa(tipo, lado, indice) {
    abrirModalBaseFuncoes(tipo, lado, indice);
    const funcao = tipo === "PICK"
        ? obterFuncaoDefinidaSlot(lado, indice)
        : "";

    filtroRota.disabled = Boolean(funcao);
    filtroRota.value = funcao;
    if (funcao) {
        modalDescricao.textContent =
            `Função definida: ${NOMES_ROTAS[funcao]}. `
            + "Somente heróis compatíveis com essa função são exibidos.";
    }
    renderizarHeroisModal();
};

selecionarHeroi = function selecionarHeroiComFuncaoFixa(heroiId) {
    if (estado.slotAtual?.tipo === "PICK") {
        const funcao = obterFuncaoDefinidaSlot(
            estado.slotAtual.lado,
            estado.slotAtual.indice
        );
        const heroi = estado.herois.find(
            (item) => Number(item.id) === Number(heroiId)
        );
        if (
            funcao
                && heroi
                && !obterRotasHeroi(heroi).includes(funcao)
        ) {
            return;
        }
    }
    selecionarHeroiBaseFuncoes(heroiId);
};

montarRequestProximoPick = function montarRequestComFuncoesAliadas() {
    const request = montarRequestProximoPickBaseFuncoes();
    request.funcoesAliadas = montarFuncoesTimeSelecionado(false);
    return request;
};

nomeRotaProximoPick = function nomeRotaAtualizado(rota) {
    return NOMES_ROTAS[rota] ?? formatarEnumBaseFuncoes(rota);
};

formatarEnum = function formatarEnumComRotas(valor) {
    return NOMES_ROTAS[valor] ?? formatarEnumBaseFuncoes(valor);
};

atualizarInferenciaFuncoes = async function atualizarInferenciaComFuncoes() {
    const versaoAtual = ++estado.versaoRequisicaoInferencia;

    estado.inferenciaCarregando = true;
    estado.inferenciaErro = "";
    renderizarHipotesesFuncao();

    const ladoSelecionado = meuLadoSelect.value;
    const funcoesSelecionadas = montarFuncoesTimeSelecionado(true);
    const corpo = {
        picksAzul: montarPicksInferencia("AZUL"),
        picksVermelho: montarPicksInferencia("VERMELHO"),
        funcoesAzul: ladoSelecionado === "AZUL"
            ? funcoesSelecionadas
            : [],
        funcoesVermelho: ladoSelecionado === "VERMELHO"
            ? funcoesSelecionadas
            : []
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
};

function criarSeletorFuncaoAliada(lado, indice, selecionada) {
    const usadas = funcoesUsadasNoTime(lado, indice);
    const opcoes = FUNCOES_SLOTS_ALIADOS.map((funcao) => {
        const selecionadaAgora = funcao.codigo === selecionada;
        const indisponivel = usadas.has(funcao.codigo) && !selecionadaAgora;
        return `
            <option
                value="${funcao.codigo}"
                ${selecionadaAgora ? "selected" : ""}
                ${indisponivel ? "disabled" : ""}
            >${funcao.nome}</option>
        `;
    }).join("");

    return `
        <label class="slot-funcao-aliada">
            <span>FUNÇÃO DO JOGADOR ${indice + 1}</span>
            <select
                data-funcao-aliada
                data-lado="${lado}"
                data-indice="${indice}"
                aria-label="Função do jogador ${indice + 1}"
            >
                <option value="">Selecionar função</option>
                ${opcoes}
            </select>
        </label>
    `;
}

function funcoesUsadasNoTime(lado, indiceIgnorado) {
    const usadas = new Set();
    const minhaOrdem = Number(minhaOrdemSelect.value);
    if (
        meuLadoSelect.value === lado
            && minhaOrdem
            && minhaOrdem - 1 !== indiceIgnorado
            && preferenciaFuncaoEquipe.value
    ) {
        usadas.add(preferenciaFuncaoEquipe.value);
    }
    estado.funcoesAliadas[lado].forEach((funcao, indice) => {
        if (indice !== indiceIgnorado && funcao) {
            usadas.add(funcao);
        }
    });
    return usadas;
}

function atualizarFuncaoAliada(lado, indice, funcao) {
    if (!estado.funcoesAliadas[lado]) {
        return;
    }
    if (funcao && funcoesUsadasNoTime(lado, indice).has(funcao)) {
        renderizarTudo();
        return;
    }
    estado.funcoesAliadas[lado][indice] = funcao;
    renderizarTudo();
    atualizarInferenciaFuncoes();
}

function obterFuncaoDefinidaSlot(lado, indice) {
    if (meuLadoSelect.value !== lado) {
        return "";
    }
    const minhaOrdem = Number(minhaOrdemSelect.value);
    if (minhaOrdem === indice + 1) {
        return preferenciaFuncaoEquipe.value;
    }
    return estado.funcoesAliadas[lado]?.[indice] ?? "";
}

function montarFuncoesTimeSelecionado(incluirUsuario) {
    const lado = meuLadoSelect.value;
    const minhaOrdem = Number(minhaOrdemSelect.value);
    if (!LADOS[lado] || !minhaOrdem) {
        return [];
    }

    const funcoes = [];
    for (let indice = 0; indice < 5; indice += 1) {
        const ordem = indice + 1;
        if (ordem === minhaOrdem) {
            if (incluirUsuario && preferenciaFuncaoEquipe.value) {
                funcoes.push({
                    ordem,
                    funcao: preferenciaFuncaoEquipe.value
                });
            }
            continue;
        }
        const funcao = estado.funcoesAliadas[lado][indice];
        if (funcao) {
            funcoes.push({ ordem, funcao });
        }
    }
    return funcoes;
}

function sincronizarContextoFuncoes() {
    const lado = meuLadoSelect.value;
    const minhaOrdem = Number(minhaOrdemSelect.value);
    if (LADOS[lado] && minhaOrdem) {
        estado.funcoesAliadas[lado][minhaOrdem - 1] = "";
        const minhaFuncao = preferenciaFuncaoEquipe.value;
        if (minhaFuncao) {
            estado.funcoesAliadas[lado] = estado.funcoesAliadas[lado]
                .map((funcao, indice) =>
                    indice !== minhaOrdem - 1 && funcao === minhaFuncao
                        ? ""
                        : funcao
                );
        }
    }
    renderizarTudo();
    atualizarInferenciaFuncoes();
}

meuLadoSelect.addEventListener("change", sincronizarContextoFuncoes);
minhaOrdemSelect.addEventListener("change", sincronizarContextoFuncoes);
preferenciaFuncaoEquipe.addEventListener("change", sincronizarContextoFuncoes);

botaoLimpar.addEventListener("click", () => {
    estado.funcoesAliadas.AZUL = Array(5).fill("");
    estado.funcoesAliadas.VERMELHO = Array(5).fill("");
    preferenciaFuncaoEquipe.value = "";
    renderizarTudo();
    atualizarInferenciaFuncoes();
});
