const ORDEM_BANS_RANQUEADA = Object.freeze([
    Object.freeze({ lado: "AZUL", indice: 0 }),
    Object.freeze({ lado: "AZUL", indice: 1 }),
    Object.freeze({ lado: "AZUL", indice: 2 }),
    Object.freeze({ lado: "VERMELHO", indice: 0 }),
    Object.freeze({ lado: "VERMELHO", indice: 1 }),
    Object.freeze({ lado: "VERMELHO", indice: 2 })
]);

const TOTAL_BANS_RANQUEADA = ORDEM_BANS_RANQUEADA.length;
const abrirModalBaseRanqueada = abrirModal;
const selecionarHeroiBaseRanqueada = selecionarHeroi;
const calcularEstadoDraftBaseRanqueada = calcularEstadoDraft;

abrirModal = function abrirModalRanqueada(tipo, lado, indice) {
    abrirModalBaseRanqueada(tipo, lado, indice);

    if (tipo === "BAN") {
        atualizarCabecalhoBanRanqueada(lado, indice);
    }
};

selecionarHeroi = function selecionarHeroiRanqueada(heroiId) {
    if (!estado.slotAtual || estado.slotAtual.tipo !== "BAN") {
        selecionarHeroiBaseRanqueada(heroiId);
        return;
    }

    const heroi = estado.herois.find(
        (item) => Number(item.id) === Number(heroiId)
    );

    if (!heroi || heroiEstaEmOutroSlot(heroi.id)) {
        return;
    }

    const slotPreenchido = { ...estado.slotAtual };
    estado.bans[slotPreenchido.lado][slotPreenchido.indice] = heroi;

    const proximoSlot = encontrarProximoBanVazio(slotPreenchido);

    if (!proximoSlot) {
        fecharModal();
        renderizarTudo();
        return;
    }

    estado.slotAtual = proximoSlot;
    pesquisaHeroi.value = "";
    filtroRota.value = "";
    botaoRemoverHeroi.classList.add("oculto");

    renderizarTudo();
    atualizarCabecalhoBanRanqueada(
        proximoSlot.lado,
        proximoSlot.indice
    );
    renderizarHeroisModal();
    pesquisaHeroi.focus();
};

heroiEstaEmOutroSlot = function heroiEstaIndisponivelRanqueada(heroiId) {
    const atual = estado.slotAtual;

    if (!atual) {
        return false;
    }

    if (atual.tipo === "BAN") {
        return heroiJaBanidoPeloMesmoLado(heroiId, atual)
            || heroiJaEscolhido(heroiId);
    }

    return listarTodosSlotsDraft().some((slot) =>
        !ehMesmoSlot(slot, atual)
        && slot.heroi
        && Number(slot.heroi.id) === Number(heroiId)
    );
};

calcularEstadoDraft = function calcularEstadoDraftRanqueada() {
    const leitura = calcularEstadoDraftBaseRanqueada();

    if (leitura.fase !== "BANS") {
        return leitura;
    }

    return {
        ...leitura,
        titulo: "Registre os seis bans revelados",
        descricao:
            `Bans simultâneos: faltam ${TOTAL_BANS_RANQUEADA - leitura.quantidadeBans}. `
            + "O mesmo herói pode aparecer uma vez em cada equipe."
    };
};

function encontrarProximoBanVazio(slotAtual) {
    const posicaoAtual = ORDEM_BANS_RANQUEADA.findIndex((slot) =>
        slot.lado === slotAtual.lado
        && slot.indice === slotAtual.indice
    );

    for (
        let deslocamento = 1;
        deslocamento <= TOTAL_BANS_RANQUEADA;
        deslocamento += 1
    ) {
        const posicao = (
            Math.max(posicaoAtual, -1) + deslocamento
        ) % TOTAL_BANS_RANQUEADA;
        const candidato = ORDEM_BANS_RANQUEADA[posicao];

        if (!estado.bans[candidato.lado][candidato.indice]) {
            return {
                tipo: "BAN",
                lado: candidato.lado,
                indice: candidato.indice
            };
        }
    }

    return null;
}

function heroiJaBanidoPeloMesmoLado(heroiId, atual) {
    return listarSlots("BAN", atual.lado).some((slot) =>
        !ehMesmoSlot(slot, atual)
        && slot.heroi
        && Number(slot.heroi.id) === Number(heroiId)
    );
}

function heroiJaEscolhido(heroiId) {
    return [
        ...listarSlots("PICK", "AZUL"),
        ...listarSlots("PICK", "VERMELHO")
    ].some((slot) =>
        slot.heroi
        && Number(slot.heroi.id) === Number(heroiId)
    );
}

function listarTodosSlotsDraft() {
    return [
        ...listarSlots("BAN", "AZUL"),
        ...listarSlots("BAN", "VERMELHO"),
        ...listarSlots("PICK", "AZUL"),
        ...listarSlots("PICK", "VERMELHO")
    ];
}

function atualizarCabecalhoBanRanqueada(lado, indice) {
    modalEtiqueta.textContent = "Modo ranqueada · bans simultâneos";
    modalTitulo.textContent =
        `${LADOS[lado].nome} · Ban ${indice + 1} de 3`;
    modalDescricao.textContent =
        "Selecione os seis bans em sequência. Cada equipe só pode banir um herói uma vez, mas as duas equipes podem repetir o mesmo ban.";
}

function ehMesmoSlot(slot, atual) {
    return atual
        && atual.tipo === slot.tipo
        && atual.lado === slot.lado
        && atual.indice === slot.indice;
}

document.addEventListener("DOMContentLoaded", () => {
    document.title = "HOK Draft Assist · Modo Ranqueada";
});
