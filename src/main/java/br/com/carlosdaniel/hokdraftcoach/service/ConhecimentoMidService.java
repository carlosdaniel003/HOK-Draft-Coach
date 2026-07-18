package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.MidDetalhadoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoMidResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.ConfiancaDado;
import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoMidLane;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilMidLane;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaMidEquipe;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.ConfrontoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.PerfilMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaMidRepository;

@Service
public class ConhecimentoMidService {

    private final HeroiService heroiService;
    private final CatalogoMidRepository catalogoMid;
    private final PerfilMidRepository perfilRepository;
    private final ConfrontoMidRepository confrontoRepository;
    private final SinergiaMidRepository sinergiaRepository;

    public ConhecimentoMidService(
        HeroiService heroiService,
        CatalogoMidRepository catalogoMid,
        PerfilMidRepository perfilRepository,
        ConfrontoMidRepository confrontoRepository,
        SinergiaMidRepository sinergiaRepository
    ) {
        this.heroiService = heroiService;
        this.catalogoMid = catalogoMid;
        this.perfilRepository = perfilRepository;
        this.confrontoRepository = confrontoRepository;
        this.sinergiaRepository = sinergiaRepository;
    }

    public List<MidDetalhadoResponse> listarMids() {
        return catalogoMid.listarTodos()
            .stream()
            .map(heroi -> detalhar(heroi, 5))
            .sorted(Comparator.comparing(
                resposta -> resposta.heroi().getNome()
            ))
            .toList();
    }

    public MidDetalhadoResponse buscarMid(String nome) {
        return detalhar(resolverMid(nome), 10);
    }

    public List<ConfrontoMidLane> melhoresRespostasParaMid(
        String nomeInimigo,
        int limite
    ) {
        Heroi inimigo = resolverMid(nomeInimigo);
        validarLimite(limite);

        return confrontoRepository.listarTodos()
            .stream()
            .filter(confronto -> nomesIguais(
                confronto.alvo(),
                inimigo.getNome()
            ))
            .sorted(comparadorConfronto())
            .limit(limite)
            .toList();
    }

    public ConfrontoMidLane analisarConfronto(
        String nomeCandidato,
        String nomeInimigo
    ) {
        Heroi candidato = resolverMid(nomeCandidato);
        Heroi inimigo = resolverMid(nomeInimigo);

        return buscarConfronto(candidato.getNome(), inimigo.getNome())
            .orElseThrow(() -> new RegraNegocioException(
                "O confronto " + candidato.getNome() + " contra "
                    + inimigo.getNome() + " ainda não possui avaliação curada."
            ));
    }

    public List<SinergiaMidEquipe> melhoresCombosDoMid(
        String nomeMid,
        int limite
    ) {
        Heroi mid = resolverMid(nomeMid);
        validarLimite(limite);

        return sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(
                sinergia.mid(),
                mid.getNome()
            ))
            .sorted(comparadorSinergia())
            .limit(limite)
            .toList();
    }

    public List<SinergiaMidEquipe> melhoresMidsParaAliado(
        String nomeAliado,
        int limite
    ) {
        Heroi aliado = resolverHeroi(nomeAliado);
        validarLimite(limite);

        return sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(
                sinergia.aliado(),
                aliado.getNome()
            ))
            .sorted(comparadorSinergia())
            .limit(limite)
            .toList();
    }

    public SinergiaMidEquipe analisarCombo(
        String nomeMid,
        String nomeAliado
    ) {
        Heroi mid = resolverMid(nomeMid);
        Heroi aliado = resolverHeroi(nomeAliado);

        return buscarSinergia(mid.getNome(), aliado.getNome())
            .orElseThrow(() -> new RegraNegocioException(
                "O combo " + mid.getNome() + " + " + aliado.getNome()
                    + " ainda não possui avaliação curada."
            ));
    }

    public List<RecomendacaoComposicaoMidResponse> recomendarContraComposicao(
        List<TipoComposicao> composicaoInimiga,
        int limite
    ) {
        if (composicaoInimiga == null || composicaoInimiga.isEmpty()) {
            throw new RegraNegocioException(
                "Informe ao menos um arquétipo da composição inimiga."
            );
        }
        validarLimite(limite);

        Set<TipoComposicao> tipos = new LinkedHashSet<>(composicaoInimiga);

        return perfilRepository.listarTodos()
            .stream()
            .map(perfil -> avaliarContra(perfil, tipos))
            .sorted(
                Comparator
                    .comparingInt(RecomendacaoComposicaoMidResponse::pontuacao)
                    .reversed()
                    .thenComparing(RecomendacaoComposicaoMidResponse::mid)
            )
            .limit(limite)
            .toList();
    }

    public int pontuarConfronto(Heroi candidato, Heroi adversario) {
        if (
            candidato == null
                || adversario == null
                || !candidato.podeJogarNaRota(Rota.MID_LANE)
                || !adversario.podeJogarNaRota(Rota.MID_LANE)
        ) {
            return 0;
        }

        Optional<ConfrontoMidLane> favoravel = buscarConfronto(
            candidato.getNome(),
            adversario.getNome()
        );
        if (favoravel.isPresent()) {
            return (favoravel.get().vantagem() - 5) * 2;
        }

        Optional<ConfrontoMidLane> desfavoravel = buscarConfronto(
            adversario.getNome(),
            candidato.getNome()
        );
        return desfavoravel
            .map(confronto -> -((confronto.vantagem() - 5) * 2))
            .orElse(0);
    }

    public int pontuarSinergia(Heroi candidato, List<Heroi> aliados) {
        if (!candidato.podeJogarNaRota(Rota.MID_LANE)) {
            return 0;
        }

        return aliados.stream()
            .map(aliado -> buscarSinergia(
                candidato.getNome(),
                aliado.getNome()
            ))
            .flatMap(Optional::stream)
            .mapToInt(sinergia -> (sinergia.nota() - 5) * 2)
            .max()
            .orElse(0);
    }

    public int pontuarRespostaAosInimigos(
        Heroi candidato,
        List<Heroi> inimigos
    ) {
        if (!candidato.podeJogarNaRota(Rota.MID_LANE) || inimigos.isEmpty()) {
            return 0;
        }

        Optional<PerfilMidLane> perfil = buscarPerfil(candidato.getNome());
        if (perfil.isEmpty()) {
            return 0;
        }

        Set<TipoComposicao> tipos = inferirComposicao(inimigos);
        int positivos = intersecao(perfil.get().quebra(), tipos).size();
        int negativos = intersecao(perfil.get().sofreContra(), tipos).size();

        return limitar(positivos * 4 - negativos * 3, -12, 12);
    }

    public Set<TipoComposicao> inferirComposicao(List<Heroi> herois) {
        Set<TipoComposicao> tipos = new LinkedHashSet<>();
        if (herois == null || herois.isEmpty()) {
            return tipos;
        }

        double controle = media(herois, AtributosHeroi::controle);
        double resistencia = media(herois, AtributosHeroi::resistencia);
        double mobilidade = media(herois, AtributosHeroi::mobilidade);
        double alcance = media(herois, AtributosHeroi::alcance);
        double sustentado = media(herois, AtributosHeroi::danoSustentado);
        long explosivos = herois.stream()
            .filter(heroi -> heroi.getAtributos().danoExplosivo() >= 8)
            .count();
        long imoveis = herois.stream()
            .filter(heroi -> heroi.getAtributos().mobilidade() <= 3)
            .count();

        if (controle >= 7) {
            tipos.add(TipoComposicao.CONTROLE_PESADO);
            tipos.add(TipoComposicao.ENGAGE_AGRUPADO);
        }
        if (resistencia >= 7) {
            tipos.add(TipoComposicao.FRONT_TO_BACK);
        }
        if (mobilidade >= 7) {
            tipos.add(TipoComposicao.ALTA_MOBILIDADE);
        }
        if (alcance >= 7) {
            tipos.add(TipoComposicao.POKE);
        }
        if (alcance >= 8.5) {
            tipos.add(TipoComposicao.ARTILHARIA);
            tipos.add(TipoComposicao.CERCO);
        }
        if (sustentado >= 8) {
            tipos.add(TipoComposicao.LUTAS_LONGAS);
        }
        if (explosivos >= 2) {
            tipos.add(TipoComposicao.EXPLOSAO);
        }
        if (imoveis >= 2) {
            tipos.add(TipoComposicao.ALVOS_IMOVEIS);
        }
        if (possuiCaracteristica(
            herois,
            "assassino",
            "dive",
            "execucao",
            "mergulho"
        )) {
            tipos.add(TipoComposicao.DIVE);
        }
        if (possuiCaracteristica(
            herois,
            "cura",
            "escudo",
            "sustentacao"
        )) {
            tipos.add(TipoComposicao.ESCUDOS_E_CURA);
        }
        if (possuiCaracteristica(
            herois,
            "pickoff",
            "gancho",
            "supressao"
        )) {
            tipos.add(TipoComposicao.PICKOFF);
        }
        if (possuiCaracteristica(
            herois,
            "wave clear",
            "invocacoes",
            "pressao de torre"
        )) {
            tipos.add(TipoComposicao.WAVE_CLEAR);
            tipos.add(TipoComposicao.PRESSAO_DE_TORRE);
        }
        if (possuiCaracteristica(
            herois,
            "rotacao",
            "teleporte",
            "acesso global"
        )) {
            tipos.add(TipoComposicao.ROTACAO);
        }
        if (possuiCaracteristica(
            herois,
            "escalamento",
            "fim de jogo"
        )) {
            tipos.add(TipoComposicao.ESCALAMENTO);
        }

        return tipos;
    }

    private MidDetalhadoResponse detalhar(Heroi heroi, int limite) {
        PerfilMidLane perfil = buscarPerfil(heroi.getNome())
            .orElseThrow(() -> new IllegalStateException(
                "Perfil estratégico ausente para " + heroi.getNome() + "."
            ));

        List<ConfrontoMidLane> vantagens = confrontoRepository.listarTodos()
            .stream()
            .filter(confronto -> nomesIguais(
                confronto.vencedor(),
                heroi.getNome()
            ))
            .sorted(comparadorConfronto())
            .limit(limite)
            .toList();

        List<ConfrontoMidLane> desvantagens = confrontoRepository.listarTodos()
            .stream()
            .filter(confronto -> nomesIguais(
                confronto.alvo(),
                heroi.getNome()
            ))
            .sorted(comparadorConfronto())
            .limit(limite)
            .toList();

        List<SinergiaMidEquipe> combos = sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(
                sinergia.mid(),
                heroi.getNome()
            ))
            .sorted(comparadorSinergia())
            .limit(limite)
            .toList();

        return new MidDetalhadoResponse(
            heroi,
            perfil,
            vantagens,
            desvantagens,
            combos
        );
    }

    private RecomendacaoComposicaoMidResponse avaliarContra(
        PerfilMidLane perfil,
        Set<TipoComposicao> inimigos
    ) {
        List<TipoComposicao> respostas = intersecao(
            perfil.quebra(),
            inimigos
        );
        List<TipoComposicao> riscos = intersecao(
            perfil.sofreContra(),
            inimigos
        );
        int pontuacao = limitar(
            50 + respostas.size() * 14 - riscos.size() * 10,
            0,
            100
        );
        List<String> motivos = new ArrayList<>();

        if (!respostas.isEmpty()) {
            motivos.add("Responde diretamente a: " + respostas + ".");
        }
        if (!riscos.isEmpty()) {
            motivos.add("Pode sofrer contra: " + riscos + ".");
        }
        if (respostas.isEmpty()) {
            motivos.add(
                "Não possui counter direto cadastrado para os tipos informados."
            );
        }

        return new RecomendacaoComposicaoMidResponse(
            perfil.heroi(),
            pontuacao,
            respostas,
            riscos,
            motivos
        );
    }

    private Heroi resolverMid(String nome) {
        return catalogoMid.buscarPorNome(nome)
            .orElseThrow(() -> new RegraNegocioException(
                "Mid laner não encontrado: " + nome + "."
            ));
    }

    private Heroi resolverHeroi(String nome) {
        return heroiService.buscarPorNome(nome)
            .orElseThrow(() -> new RegraNegocioException(
                "Herói não encontrado: " + nome + "."
            ));
    }

    private Optional<PerfilMidLane> buscarPerfil(String nome) {
        return perfilRepository.listarTodos()
            .stream()
            .filter(perfil -> nomesIguais(perfil.heroi(), nome))
            .findFirst();
    }

    private Optional<ConfrontoMidLane> buscarConfronto(
        String vencedor,
        String alvo
    ) {
        return confrontoRepository.listarTodos()
            .stream()
            .filter(confronto -> nomesIguais(
                confronto.vencedor(),
                vencedor
            ))
            .filter(confronto -> nomesIguais(confronto.alvo(), alvo))
            .findFirst();
    }

    private Optional<SinergiaMidEquipe> buscarSinergia(
        String mid,
        String aliado
    ) {
        return sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(sinergia.mid(), mid))
            .filter(sinergia -> nomesIguais(sinergia.aliado(), aliado))
            .findFirst();
    }

    private Comparator<ConfrontoMidLane> comparadorConfronto() {
        Comparator<ConfrontoMidLane> porConfianca = Comparator
            .comparingInt(
                (ConfrontoMidLane confronto) -> pesoConfianca(
                    confronto.confianca()
                )
            )
            .reversed();

        return Comparator
            .comparingInt(ConfrontoMidLane::vantagem)
            .reversed()
            .thenComparing(porConfianca)
            .thenComparing(ConfrontoMidLane::vencedor)
            .thenComparing(ConfrontoMidLane::alvo);
    }

    private Comparator<SinergiaMidEquipe> comparadorSinergia() {
        Comparator<SinergiaMidEquipe> porConfianca = Comparator
            .comparingInt(
                (SinergiaMidEquipe sinergia) -> pesoConfianca(
                    sinergia.confianca()
                )
            )
            .reversed();

        return Comparator
            .comparingInt(SinergiaMidEquipe::nota)
            .reversed()
            .thenComparing(porConfianca)
            .thenComparing(SinergiaMidEquipe::mid)
            .thenComparing(SinergiaMidEquipe::aliado);
    }

    private int pesoConfianca(ConfiancaDado confianca) {
        return switch (confianca) {
            case ALTA -> 3;
            case MEDIA -> 2;
            case EXPLORATORIA -> 1;
        };
    }

    private List<TipoComposicao> intersecao(
        List<TipoComposicao> valores,
        Set<TipoComposicao> filtro
    ) {
        return valores.stream().filter(filtro::contains).toList();
    }

    private boolean possuiCaracteristica(
        List<Heroi> herois,
        String... procuradas
    ) {
        Set<String> termos = new LinkedHashSet<>();
        for (String procurada : procuradas) {
            termos.add(normalizar(procurada));
        }

        return herois.stream()
            .flatMap(heroi -> heroi.getCaracteristicas().stream())
            .map(this::normalizar)
            .anyMatch(caracteristica -> termos.stream().anyMatch(
                caracteristica::contains
            ));
    }

    private double media(
        List<Heroi> herois,
        ToIntFunction<AtributosHeroi> extrator
    ) {
        return herois.stream()
            .map(Heroi::getAtributos)
            .mapToInt(extrator)
            .average()
            .orElse(0);
    }

    private boolean nomesIguais(String primeiro, String segundo) {
        return normalizar(primeiro).equals(normalizar(segundo));
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
