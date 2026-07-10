const ORDEM_BANS_RANQUEADA = [
    { lado: "AZUL", indice: 0 },
    { lado: "AZUL", indice: 1 },
    { lado: "AZUL", indice: 2 },
    { lado: "VERMELHO", indice: 0 },
    { lado: "VERMELHO", indice: 1 },
    { lado: "VERMELHO", indice: 2 }
];

const abrirModalBaseRanqueada = abrirModal;
const selecionarHeroiBaseRanqueada = selecionarHeroi;
const calcularEstadoDraftBaseRanqueada = calcularEstadoDraft;

abrirModal = function abrirModalRanqueada(tipo, lado, indice) {
    abrirModalBaseRanqueada(tipo, lado, indice);

    if (tipo !== "BAN") {
        return;
    }

    atualizarCabecalhoBanRanqueada(lado, indice);
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
    renderizarTudo();

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
        const repetidoNoMesmoLado = listarSlots("BAN", atual.lado)
            .some((slot) =>
                !ehMesmoSlot(slot, atual)
                && slot.heroi
                && Number(slot.heroi.id) === Number(heroiId)
            );

        const jaEscolhido = [
            ...listarSlots("PICK", "AZUL"),
            ...listarSlots("PICK", "VERMELHO")
        ].some((slot) =>
            slot.heroi
            && Number(slot.heroi.id) === Number(heroiId)
        );

        return repetidoNoMesmoLado || jaEscolhido;
    }

    return [
        ...listarSlots("BAN", "AZUL"),
        ...listarSlots("BAN", "VERMELHO"),
        ...listarSlots("PICK", "AZUL"),
        ...listarSlots("PICK", "VERMELHO")
    ].some((slot) =>
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
            `Bans simultâneos: faltam ${6 - leitura.quantidadeBans}. `
            + "O mesmo herói pode aparecer uma vez em cada equipe."
    };
};

function encontrarProximoBanVazio(slotAtual) {
    const posicaoAtual = ORDEM_BANS_RANQUEADA.findIndex((slot) =>
        slot.lado === slotAtual.lado
        && slot.indice === slotAtual.indice
    );

    for (let deslocamento = 1; deslocamento <= ORDEM_BANS_RANQUEADA.length; deslocamento += 1) {
        const posicao = (
            Math.max(posicaoAtual, -1) + deslocamento
        ) % ORDEM_BANS_RANQUEADA.length;
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

function atualizarCabecalhoBanRanqueada(lado, indice) {
    modalEtiqueta.textContent = "Modo ranqueada · bans simultâneos";
    modalTitulo.textContent =
        `${LADOS[lado].nome} · Ban ${indice + 1}`;
    modalDescricao.textContent =
        "Selecione os seis bans em sequência. Cada equipe só pode banir um herói uma vez, mas os dois lados podem repetir o mesmo ban.";
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
