package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoEstrategico;
import br.com.carlosdaniel.hokdraftcoach.dto.PrioridadeDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DistribuicaoDano;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.NivelDna;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.SeveridadeDiagnostico;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.TipoDano;

@Service
public class DnaComposicaoService {

    private static final double[] PESOS_AGREGACAO = {
        0.60, 0.23, 0.10, 0.05, 0.02
    };

    private final HeroiService heroiService;
    private final DnaHeroiService dnaHeroiService;

    public DnaComposicaoService(
        HeroiService heroiService,
        DnaHeroiService dnaHeroiService
    ) {
        this.heroiService = heroiService;
        this.dnaHeroiService = dnaHeroiService;
    }

    public DiagnosticoComposicaoResponse diagnosticar(
        List<String> aliados,
        List<String> inimigos
    ) {
        List<Heroi> nossaEquipe = resolverEquipe(aliados, "aliada");
        List<Heroi> equipeInimiga = resolverEquipeOpcional(inimigos, "inimiga");

        DnaComposicao nossoDna = gerarDna(nossaEquipe);
        DnaComposicao dnaInimigo = gerarDna(equipeInimiga);
        List<DiagnosticoEstrategico> diagnosticos = new ArrayList<>();
        EnumMap<DimensaoEstrategica, PrioridadeDraftResponse> prioridades =
            new EnumMap<>(DimensaoEstrategica.class);

        diagnosticarEstruturaPropria(nossoDna, diagnosticos, prioridades);
        diagnosticarConfronto(
            nossoDna,
            dnaInimigo,
            diagnosticos,
            prioridades
        );

        if (diagnosticos.isEmpty()) {
            diagnosticos.add(new DiagnosticoEstrategico(
                "COMPOSICAO_EQUILIBRADA",
                SeveridadeDiagnostico.INFO,
                20,
                "Composição estruturalmente equilibrada",
                "Não foi encontrado déficit estratégico crítico nas escolhas atuais.",
                List.of()
            ));
        }

        List<DiagnosticoEstrategico> ordenados = diagnosticos.stream()
            .sorted(
                Comparator.comparingInt(DiagnosticoEstrategico::prioridade)
                    .reversed()
            )
            .toList();
        List<PrioridadeDraftResponse> prioridadesOrdenadas = prioridades
            .values()
            .stream()
            .sorted(
                Comparator.comparingInt(PrioridadeDraftResponse::urgencia)
                    .reversed()
            )
            .toList();

        return new DiagnosticoComposicaoResponse(
            nossoDna,
            dnaInimigo,
            ordenados,
            prioridadesOrdenadas,
            true
        );
    }

    public DnaComposicao gerarDnaPorNomes(List<String> nomes) {
        return gerarDna(resolverEquipe(nomes, "informada"));
    }

    public List<RecomendacaoDnaResponse> recomendar(
        List<String> aliados,
        List<String> inimigos,
        Rota rota,
        int limite
    ) {
        validarLimite(limite);
        DiagnosticoComposicaoResponse diagnostico = diagnosticar(
            aliados,
            inimigos
        );
        List<Heroi> nossaEquipe = resolverEquipe(aliados, "aliada");
        Set<String> jaEscolhidos = new LinkedHashSet<>();
        nossaEquipe.forEach(heroi -> jaEscolhidos.add(normalizar(heroi.getNome())));
        resolverEquipeOpcional(inimigos, "inimiga")
            .forEach(heroi -> jaEscolhidos.add(normalizar(heroi.getNome())));

        return heroiService.listarPorRota(rota)
            .stream()
            .filter(heroi -> !jaEscolhidos.contains(normalizar(heroi.getNome())))
            .map(heroi -> avaliarCandidato(
                heroi,
                nossaEquipe,
                diagnostico
            ))
            .sorted(
                Comparator.comparingInt(RecomendacaoDnaResponse::pontuacao)
                    .reversed()
                    .thenComparing(RecomendacaoDnaResponse::heroi)
            )
            .limit(limite)
            .toList();
    }

    public DnaComposicao gerarDna(List<Heroi> equipe) {
        EnumMap<DimensaoEstrategica, Integer> vetor =
            new EnumMap<>(DimensaoEstrategica.class);
        EnumMap<DimensaoEstrategica, NivelDna> niveis =
            new EnumMap<>(DimensaoEstrategica.class);
        List<DnaHeroi> individuais = equipe.stream()
            .map(dnaHeroiService::calcular)
            .toList();

        for (DimensaoEstrategica dimensao : DimensaoEstrategica.values()) {
            List<Integer> valores = individuais.stream()
                .map(dna -> dna.valor(dimensao))
                .sorted(Comparator.reverseOrder())
                .toList();
            int agregado = agregar(valores);
            vetor.put(dimensao, agregado);
            niveis.put(dimensao, NivelDna.classificar(agregado));
        }

        return new DnaComposicao(
            equipe.stream().map(Heroi::getNome).toList(),
            vetor,
            niveis,
            calcularDistribuicaoDano(equipe),
            contarTanques(equipe),
            contarCarregadoresFrageis(equipe),
            inferirArquetipos(vetor)
        );
    }

    private RecomendacaoDnaResponse avaliarCandidato(
        Heroi candidato,
        List<Heroi> nossaEquipe,
        DiagnosticoComposicaoResponse diagnostico
    ) {
        DnaHeroi dnaCandidato = dnaHeroiService.calcular(candidato);
        List<Heroi> projetada = new ArrayList<>(nossaEquipe);
        projetada.add(candidato);
        DnaComposicao dnaProjetado = gerarDna(projetada);
        DnaComposicao atual = diagnostico.nossaComposicao();
        DnaComposicao inimigo = diagnostico.composicaoInimiga();
        List<DimensaoEstrategica> corrige = new ArrayList<>();
        List<DimensaoEstrategica> explora = new ArrayList<>();
        List<String> motivos = new ArrayList<>();
        double pontosCorrecao = 0;

        for (PrioridadeDraftResponse prioridade : diagnostico.prioridades()) {
            int antes = atual.valor(prioridade.dimensao());
            int depois = dnaProjetado.valor(prioridade.dimensao());
            int ganho = Math.max(0, depois - antes);
            if (ganho >= 5) {
                corrige.add(prioridade.dimensao());
                pontosCorrecao += ganho * (prioridade.urgencia() / 100.0);
                motivos.add(
                    "Eleva " + prioridade.dimensao() + " de " + antes
                        + " para " + depois + "."
                );
            }
        }

        double pontosExploracao = 0;
        pontosExploracao += explorar(
            inimigo,
            dnaCandidato,
            DimensaoEstrategica.LINHA_DE_FRENTE,
            DimensaoEstrategica.ANTI_TANQUE,
            explora,
            motivos,
            "Explora a linha de frente inimiga com dano anti-tanque."
        );
        pontosExploracao += explorar(
            inimigo,
            dnaCandidato,
            DimensaoEstrategica.SUSTAIN,
            DimensaoEstrategica.ANTI_CURA,
            explora,
            motivos,
            "Responde à sustentação inimiga com anti-cura."
        );
        pontosExploracao += explorar(
            inimigo,
            dnaCandidato,
            DimensaoEstrategica.POKE,
            DimensaoEstrategica.ENGAGE,
            explora,
            motivos,
            "Consegue iniciar sobre uma composição de poke."
        );
        pontosExploracao += explorar(
            inimigo,
            dnaCandidato,
            DimensaoEstrategica.DIVE,
            DimensaoEstrategica.PEEL,
            explora,
            motivos,
            "Acrescenta peel contra o dive inimigo."
        );

        int versatilidade = DimensaoEstrategica.values().length == 0
            ? 0
            : (int) Math.round(
                dnaCandidato.vetor().values().stream()
                    .filter(valor -> valor >= 65)
                    .count() * 1.5
            );
        int pontuacao = limitar(
            (int) Math.round(35 + pontosCorrecao * 1.6 + pontosExploracao)
                + versatilidade,
            0,
            100
        );

        if (motivos.isEmpty()) {
            motivos.add(
                "Encaixe neutro: não corrige os principais déficits atuais."
            );
        }

        return new RecomendacaoDnaResponse(
            candidato.getNome(),
            candidato.getRota(),
            pontuacao,
            corrige.stream().distinct().toList(),
            explora.stream().distinct().toList(),
            motivos.stream().distinct().limit(6).toList()
        );
    }

    private double explorar(
        DnaComposicao inimigo,
        DnaHeroi candidato,
        DimensaoEstrategica ameaca,
        DimensaoEstrategica resposta,
        List<DimensaoEstrategica> explora,
        List<String> motivos,
        String motivo
    ) {
        int valorAmeaca = inimigo.valor(ameaca);
        int valorResposta = candidato.valor(resposta);
        if (valorAmeaca >= 60 && valorResposta >= 60) {
            explora.add(resposta);
            motivos.add(motivo);
            return ((valorAmeaca - 50) + (valorResposta - 50)) * 0.18;
        }
        return 0;
    }

    private void diagnosticarEstruturaPropria(
        DnaComposicao nosso,
        List<DiagnosticoEstrategico> diagnosticos,
        Map<DimensaoEstrategica, PrioridadeDraftResponse> prioridades
    ) {
        int dano = Math.max(
            nosso.valor(DimensaoEstrategica.DPS),
            nosso.valor(DimensaoEstrategica.EXPLOSAO)
        );

        if (dano >= 65 && nosso.valor(DimensaoEstrategica.ENGAGE) < 42) {
            adicionar(
                diagnosticos,
                "DANO_SEM_INICIACAO",
                SeveridadeDiagnostico.CRITICA,
                95,
                "A composição possui dano, mas não consegue iniciar",
                "Os carregadores conseguem causar dano, porém falta uma ferramenta confiável para começar a luta.",
                DimensaoEstrategica.ENGAGE
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.ENGAGE,
                95,
                nosso.valor(DimensaoEstrategica.ENGAGE),
                60,
                "Adicionar iniciação confiável antes de reforçar mais dano."
            );
        }

        if (nosso.valor(DimensaoEstrategica.ENGAGE) >= 65 && dano < 48) {
            adicionar(
                diagnosticos,
                "INICIACAO_SEM_DANO",
                SeveridadeDiagnostico.ALTA,
                85,
                "A composição inicia, mas não possui follow-up suficiente",
                "Existe engage, porém falta dano para converter o controle em eliminações.",
                DimensaoEstrategica.DPS,
                DimensaoEstrategica.EXPLOSAO
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.DPS,
                85,
                nosso.valor(DimensaoEstrategica.DPS),
                60,
                "Adicionar dano sustentado para acompanhar a iniciação."
            );
        }

        if (nosso.valor(DimensaoEstrategica.LINHA_DE_FRENTE) < 42) {
            adicionar(
                diagnosticos,
                "SEM_LINHA_DE_FRENTE",
                SeveridadeDiagnostico.ALTA,
                82,
                "A equipe não possui linha de frente suficiente",
                "A composição terá dificuldade para ocupar espaço, contestar objetivos e proteger os carregadores.",
                DimensaoEstrategica.LINHA_DE_FRENTE
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.LINHA_DE_FRENTE,
                82,
                nosso.valor(DimensaoEstrategica.LINHA_DE_FRENTE),
                58,
                "Adicionar um herói capaz de absorver a primeira rotação."
            );
        }

        if (
            nosso.quantidadeCarregadoresFrageis() >= 2
                && nosso.valor(DimensaoEstrategica.PEEL) < 45
        ) {
            adicionar(
                diagnosticos,
                "CARRIES_SEM_PROTECAO",
                SeveridadeDiagnostico.ALTA,
                80,
                "Há múltiplos carregadores frágeis sem peel",
                "A equipe concentra dano na retaguarda, mas não possui controle defensivo suficiente.",
                DimensaoEstrategica.PEEL,
                DimensaoEstrategica.PROTECAO
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.PEEL,
                80,
                nosso.valor(DimensaoEstrategica.PEEL),
                58,
                "Adicionar proteção ou controle defensivo para a backline."
            );
        }

        if (nosso.valor(DimensaoEstrategica.WAVE_CLEAR) < 38) {
            adicionar(
                diagnosticos,
                "WAVE_CLEAR_BAIXO",
                SeveridadeDiagnostico.ATENCAO,
                62,
                "A composição possui limpeza de ondas insuficiente",
                "Será difícil defender cerco, recuperar prioridade e preparar objetivos.",
                DimensaoEstrategica.WAVE_CLEAR
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.WAVE_CLEAR,
                62,
                nosso.valor(DimensaoEstrategica.WAVE_CLEAR),
                52,
                "Adicionar wave clear para estabilizar o mapa."
            );
        }

        if (nosso.valor(DimensaoEstrategica.OBJETIVOS) < 38) {
            adicionar(
                diagnosticos,
                "CONTROLE_DE_OBJETIVOS_BAIXO",
                SeveridadeDiagnostico.ATENCAO,
                58,
                "A equipe possui baixo controle de objetivos",
                "Falta dano, resistência ou zoneamento para assegurar Tyrant, Overlord e estruturas.",
                DimensaoEstrategica.OBJETIVOS
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.OBJETIVOS,
                58,
                nosso.valor(DimensaoEstrategica.OBJETIVOS),
                52,
                "Adicionar presença em objetivos e dano sustentado."
            );
        }

        if (
            nosso.distribuicaoDano().fisico() >= 80
                || nosso.distribuicaoDano().magico() >= 80
        ) {
            adicionar(
                diagnosticos,
                "DANO_MONOTIPO",
                SeveridadeDiagnostico.ATENCAO,
                60,
                "A distribuição de dano está muito concentrada",
                "O inimigo poderá responder com uma única linha de resistência defensiva.",
                DimensaoEstrategica.DPS,
                DimensaoEstrategica.EXPLOSAO
            );
        }
    }

    private void diagnosticarConfronto(
        DnaComposicao nosso,
        DnaComposicao inimigo,
        List<DiagnosticoEstrategico> diagnosticos,
        Map<DimensaoEstrategica, PrioridadeDraftResponse> prioridades
    ) {
        if (inimigo.herois().isEmpty()) {
            return;
        }

        if (
            (inimigo.quantidadeTanques() >= 3
                || inimigo.valor(DimensaoEstrategica.LINHA_DE_FRENTE) >= 72)
                && (nosso.valor(DimensaoEstrategica.DPS) < 60
                    || nosso.valor(DimensaoEstrategica.ANTI_TANQUE) < 55)
        ) {
            adicionar(
                diagnosticos,
                "SEM_RESPOSTA_A_TANQUES",
                SeveridadeDiagnostico.CRITICA,
                100,
                "O inimigo possui muita linha de frente e falta dano sustentado",
                "A composição inimiga tem " + inimigo.quantidadeTanques()
                    + " tanques ou equivalentes, enquanto nossa equipe não possui DPS e anti-tanque suficientes.",
                DimensaoEstrategica.DPS,
                DimensaoEstrategica.ANTI_TANQUE
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.ANTI_TANQUE,
                100,
                nosso.valor(DimensaoEstrategica.ANTI_TANQUE),
                68,
                "Priorizar dano verdadeiro, percentual ou execução contra tanques."
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.DPS,
                92,
                nosso.valor(DimensaoEstrategica.DPS),
                65,
                "Adicionar dano sustentado para atravessar a frontline."
            );
        }

        if (
            inimigo.valor(DimensaoEstrategica.DIVE) >= 65
                && Math.max(
                    nosso.valor(DimensaoEstrategica.PEEL),
                    nosso.valor(DimensaoEstrategica.DESENGAGE)
                ) < 50
        ) {
            adicionar(
                diagnosticos,
                "VULNERAVEL_A_DIVE",
                SeveridadeDiagnostico.CRITICA,
                94,
                "A backline está vulnerável ao dive inimigo",
                "O inimigo possui acesso aos carregadores e nossa equipe não tem peel ou desengage suficiente.",
                DimensaoEstrategica.PEEL,
                DimensaoEstrategica.DESENGAGE
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.PEEL,
                94,
                nosso.valor(DimensaoEstrategica.PEEL),
                62,
                "Adicionar controle defensivo para interromper mergulhadores."
            );
        }

        if (
            inimigo.valor(DimensaoEstrategica.POKE) >= 65
                && nosso.valor(DimensaoEstrategica.SUSTAIN) < 50
                && nosso.valor(DimensaoEstrategica.ENGAGE) < 55
        ) {
            adicionar(
                diagnosticos,
                "SEM_RESPOSTA_A_POKE",
                SeveridadeDiagnostico.ALTA,
                86,
                "A equipe pode ser desgastada antes de conseguir lutar",
                "Contra o poke inimigo, é necessário sustain para absorver dano ou engage para fechar a distância.",
                DimensaoEstrategica.SUSTAIN,
                DimensaoEstrategica.ENGAGE
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.ENGAGE,
                84,
                nosso.valor(DimensaoEstrategica.ENGAGE),
                60,
                "Adicionar iniciação para impedir o cerco prolongado."
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.SUSTAIN,
                76,
                nosso.valor(DimensaoEstrategica.SUSTAIN),
                55,
                "Adicionar cura, escudos ou recuperação contra poke."
            );
        }

        if (
            inimigo.valor(DimensaoEstrategica.SUSTAIN) >= 65
                && nosso.valor(DimensaoEstrategica.ANTI_CURA) < 42
        ) {
            adicionar(
                diagnosticos,
                "SEM_ANTI_CURA",
                SeveridadeDiagnostico.ALTA,
                88,
                "O inimigo possui muita sustentação e falta anti-cura",
                "Curas, escudos e roubo de vida inimigos podem prolongar lutas além do nosso dano disponível.",
                DimensaoEstrategica.ANTI_CURA
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.ANTI_CURA,
                88,
                nosso.valor(DimensaoEstrategica.ANTI_CURA),
                70,
                "Priorizar herói com redução de cura ou execução."
            );
        }

        if (
            inimigo.valor(DimensaoEstrategica.PRESSAO_LATERAL) >= 70
                && nosso.valor(DimensaoEstrategica.PRESSAO_LATERAL) < 48
                && nosso.valor(DimensaoEstrategica.WAVE_CLEAR) < 55
        ) {
            adicionar(
                diagnosticos,
                "SEM_RESPOSTA_A_SPLIT_PUSH",
                SeveridadeDiagnostico.ALTA,
                80,
                "A equipe não possui resposta à pressão lateral inimiga",
                "O inimigo pode dividir o mapa enquanto nossa composição depende de agrupar.",
                DimensaoEstrategica.PRESSAO_LATERAL,
                DimensaoEstrategica.WAVE_CLEAR
            );
            prioridade(
                prioridades,
                DimensaoEstrategica.PRESSAO_LATERAL,
                80,
                nosso.valor(DimensaoEstrategica.PRESSAO_LATERAL),
                58,
                "Adicionar duelista lateral, resposta global ou wave clear."
            );
        }
    }

    private int agregar(List<Integer> valores) {
        if (valores.isEmpty()) {
            return 0;
        }
        double soma = 0;
        double pesosUsados = 0;
        int limite = Math.min(valores.size(), PESOS_AGREGACAO.length);
        for (int indice = 0; indice < limite; indice++) {
            soma += valores.get(indice) * PESOS_AGREGACAO[indice];
            pesosUsados += PESOS_AGREGACAO[indice];
        }
        return limitar((int) Math.round(soma / pesosUsados), 0, 100);
    }

    private DistribuicaoDano calcularDistribuicaoDano(List<Heroi> equipe) {
        if (equipe.isEmpty()) {
            return new DistribuicaoDano(0, 0, 0);
        }
        long fisico = equipe.stream()
            .filter(heroi -> heroi.getTipoDano() == TipoDano.FISICO)
            .count();
        long magico = equipe.stream()
            .filter(heroi -> heroi.getTipoDano() == TipoDano.MAGICO)
            .count();
        long misto = equipe.stream()
            .filter(heroi -> heroi.getTipoDano() == TipoDano.MISTO)
            .count();
        int total = equipe.size();
        return new DistribuicaoDano(
            (int) Math.round(fisico * 100.0 / total),
            (int) Math.round(magico * 100.0 / total),
            (int) Math.round(misto * 100.0 / total)
        );
    }

    private int contarTanques(List<Heroi> equipe) {
        return (int) equipe.stream()
            .filter(heroi ->
                heroi.getClasse() == ClasseHeroi.TANQUE
                    || heroi.getAtributos().resistencia() >= 8
                    || possuiTag(heroi, "tanque", "linha de frente", "frontline")
            )
            .count();
    }

    private int contarCarregadoresFrageis(List<Heroi> equipe) {
        return (int) equipe.stream()
            .filter(heroi ->
                (heroi.getClasse() == ClasseHeroi.ATIRADOR
                    || heroi.getClasse() == ClasseHeroi.MAGO)
                    && heroi.getAtributos().resistencia() <= 4
            )
            .count();
    }

    private List<TipoComposicao> inferirArquetipos(
        Map<DimensaoEstrategica, Integer> vetor
    ) {
        List<TipoComposicao> tipos = new ArrayList<>();
        int engage = valor(vetor, DimensaoEstrategica.ENGAGE);
        int controle = valor(vetor, DimensaoEstrategica.CONTROLE);
        int poke = valor(vetor, DimensaoEstrategica.POKE);
        int dps = valor(vetor, DimensaoEstrategica.DPS);
        int explosao = valor(vetor, DimensaoEstrategica.EXPLOSAO);
        int frontline = valor(vetor, DimensaoEstrategica.LINHA_DE_FRENTE);
        int sustain = valor(vetor, DimensaoEstrategica.SUSTAIN);
        int peel = valor(vetor, DimensaoEstrategica.PEEL);
        int desengage = valor(vetor, DimensaoEstrategica.DESENGAGE);
        int mobilidade = valor(vetor, DimensaoEstrategica.MOBILIDADE);
        int alcance = valor(vetor, DimensaoEstrategica.ALCANCE);
        int lateral = valor(vetor, DimensaoEstrategica.PRESSAO_LATERAL);
        int dive = valor(vetor, DimensaoEstrategica.DIVE);
        int scaling = valor(vetor, DimensaoEstrategica.ESCALAMENTO);

        adicionarSe(tipos, TipoComposicao.ENGAGE_AGRUPADO,
            engage >= 65 && controle >= 55);
        adicionarSe(tipos, TipoComposicao.POKE, poke >= 65);
        adicionarSe(tipos, TipoComposicao.EXPLOSAO, explosao >= 70);
        adicionarSe(tipos, TipoComposicao.LUTAS_LONGAS,
            dps >= 65 && sustain >= 55);
        adicionarSe(tipos, TipoComposicao.FRONT_TO_BACK,
            frontline >= 60 && peel >= 50 && dps >= 55);
        adicionarSe(tipos, TipoComposicao.HIPERCARREGADOR,
            dps >= 72 && scaling >= 70);
        adicionarSe(tipos, TipoComposicao.ESCUDOS_E_CURA,
            sustain >= 65 || valor(vetor, DimensaoEstrategica.PROTECAO) >= 68);
        adicionarSe(tipos, TipoComposicao.ALTA_MOBILIDADE, mobilidade >= 68);
        adicionarSe(tipos, TipoComposicao.CONTROLE_PESADO, controle >= 70);
        adicionarSe(tipos, TipoComposicao.SPLIT_PUSH, lateral >= 70);
        adicionarSe(tipos, TipoComposicao.PICKOFF,
            controle >= 55 && explosao >= 65 && mobilidade >= 55);
        adicionarSe(tipos, TipoComposicao.DESENGAGE, desengage >= 65);
        adicionarSe(tipos, TipoComposicao.CERCO,
            poke >= 65 && valor(vetor, DimensaoEstrategica.WAVE_CLEAR) >= 60);
        adicionarSe(tipos, TipoComposicao.WAVE_CLEAR,
            valor(vetor, DimensaoEstrategica.WAVE_CLEAR) >= 65);
        adicionarSe(tipos, TipoComposicao.OBJETIVOS,
            valor(vetor, DimensaoEstrategica.OBJETIVOS) >= 65);
        adicionarSe(tipos, TipoComposicao.ROTACAO, mobilidade >= 65);
        adicionarSe(tipos, TipoComposicao.ARTILHARIA,
            poke >= 75 && alcance >= 75);
        adicionarSe(tipos, TipoComposicao.ANTI_DIVE,
            peel >= 65 || desengage >= 68);
        adicionarSe(tipos, TipoComposicao.ESCALAMENTO, scaling >= 70);
        adicionarSe(tipos, TipoComposicao.ANTI_TANQUE,
            valor(vetor, DimensaoEstrategica.ANTI_TANQUE) >= 65);
        adicionarSe(tipos, TipoComposicao.DIVE, dive >= 65);

        return List.copyOf(tipos);
    }

    private List<Heroi> resolverEquipe(List<String> nomes, String descricao) {
        if (nomes == null || nomes.isEmpty()) {
            throw new RegraNegocioException(
                "Informe ao menos um herói para a composição " + descricao + "."
            );
        }
        return resolverEquipeInterna(nomes, descricao);
    }

    private List<Heroi> resolverEquipeOpcional(
        List<String> nomes,
        String descricao
    ) {
        if (nomes == null || nomes.isEmpty()) {
            return List.of();
        }
        return resolverEquipeInterna(nomes, descricao);
    }

    private List<Heroi> resolverEquipeInterna(
        List<String> nomes,
        String descricao
    ) {
        if (nomes.size() > 5) {
            throw new RegraNegocioException(
                "A composição " + descricao + " pode ter no máximo 5 heróis."
            );
        }
        List<Heroi> equipe = nomes.stream()
            .map(String::trim)
            .filter(nome -> !nome.isBlank())
            .map(nome -> heroiService.buscarPorNome(nome)
                .orElseThrow(() -> new RegraNegocioException(
                    "Herói não encontrado: " + nome + "."
                )))
            .toList();
        long unicos = equipe.stream()
            .map(Heroi::getNome)
            .map(this::normalizar)
            .distinct()
            .count();
        if (unicos != equipe.size()) {
            throw new RegraNegocioException(
                "A composição " + descricao + " possui heróis repetidos."
            );
        }
        return equipe;
    }

    private void adicionar(
        List<DiagnosticoEstrategico> diagnosticos,
        String codigo,
        SeveridadeDiagnostico severidade,
        int prioridade,
        String titulo,
        String descricao,
        DimensaoEstrategica... dimensoes
    ) {
        diagnosticos.add(new DiagnosticoEstrategico(
            codigo,
            severidade,
            prioridade,
            titulo,
            descricao,
            List.of(dimensoes)
        ));
    }

    private void prioridade(
        Map<DimensaoEstrategica, PrioridadeDraftResponse> prioridades,
        DimensaoEstrategica dimensao,
        int urgencia,
        int atual,
        int alvo,
        String motivo
    ) {
        PrioridadeDraftResponse existente = prioridades.get(dimensao);
        if (existente == null || urgencia > existente.urgencia()) {
            prioridades.put(dimensao, new PrioridadeDraftResponse(
                dimensao,
                urgencia,
                atual,
                alvo,
                motivo
            ));
        }
    }

    private boolean possuiTag(Heroi heroi, String... tags) {
        return heroi.getCaracteristicas()
            .stream()
            .map(this::normalizar)
            .anyMatch(caracteristica -> {
                for (String tag : tags) {
                    if (caracteristica.contains(normalizar(tag))) {
                        return true;
                    }
                }
                return false;
            });
    }

    private int valor(
        Map<DimensaoEstrategica, Integer> vetor,
        DimensaoEstrategica dimensao
    ) {
        return vetor.getOrDefault(dimensao, 0);
    }

    private void adicionarSe(
        List<TipoComposicao> tipos,
        TipoComposicao tipo,
        boolean condicao
    ) {
        if (condicao) {
            tipos.add(tipo);
        }
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase(Locale.ROOT);
    }

    private void validarLimite(int limite) {
        if (limite < 1 || limite > 50) {
            throw new RegraNegocioException(
                "O limite deve estar entre 1 e 50."
            );
        }
    }

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
