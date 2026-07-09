package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoSuporteResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.SuporteDetalhadoResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.ConfiancaDado;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilSuporte;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaBotLane;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.PerfilSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaBotLaneRepository;

@Service
public class ConhecimentoSuporteService {

    private final HeroiService heroiService;
    private final CatalogoSuporteRepository catalogoSuporte;
    private final PerfilSuporteRepository perfilRepository;
    private final SinergiaBotLaneRepository sinergiaRepository;

    public ConhecimentoSuporteService(
        HeroiService heroiService,
        CatalogoSuporteRepository catalogoSuporte,
        PerfilSuporteRepository perfilRepository,
        SinergiaBotLaneRepository sinergiaRepository
    ) {
        this.heroiService = heroiService;
        this.catalogoSuporte = catalogoSuporte;
        this.perfilRepository = perfilRepository;
        this.sinergiaRepository = sinergiaRepository;
    }

    public List<SuporteDetalhadoResponse> listarSuportes() {
        return catalogoSuporte.listarTodos()
            .stream()
            .map(heroi -> detalhar(heroi, 5))
            .sorted(Comparator.comparing(resposta -> resposta.heroi().getNome()))
            .toList();
    }

    public SuporteDetalhadoResponse buscarSuporte(String nome) {
        Heroi heroi = resolverSuporte(nome);
        return detalhar(heroi, 10);
    }

    public List<SinergiaBotLane> melhoresSuportesParaAtirador(
        String nomeAtirador,
        int limite
    ) {
        Heroi atirador = resolverAtirador(nomeAtirador);
        validarLimite(limite);

        return sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(sinergia.atirador(), atirador.getNome()))
            .sorted(comparadorSinergia())
            .limit(limite)
            .toList();
    }

    public List<SinergiaBotLane> melhoresAtiradoresParaSuporte(
        String nomeSuporte,
        int limite
    ) {
        Heroi suporte = resolverSuporte(nomeSuporte);
        validarLimite(limite);

        return sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(sinergia.suporte(), suporte.getNome()))
            .sorted(comparadorSinergia())
            .limit(limite)
            .toList();
    }

    public SinergiaBotLane analisarDupla(
        String nomeSuporte,
        String nomeAtirador
    ) {
        Heroi suporte = resolverSuporte(nomeSuporte);
        Heroi atirador = resolverAtirador(nomeAtirador);

        return buscarSinergia(suporte.getNome(), atirador.getNome())
            .orElseThrow(() -> new RegraNegocioException(
                "A dupla " + suporte.getNome() + " + " + atirador.getNome()
                    + " ainda não possui avaliação curada."
            ));
    }

    public List<RecomendacaoComposicaoSuporteResponse> recomendarContraComposicao(
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
                    .comparingInt(RecomendacaoComposicaoSuporteResponse::pontuacao)
                    .reversed()
                    .thenComparing(RecomendacaoComposicaoSuporteResponse::suporte)
            )
            .limit(limite)
            .toList();
    }

    public int pontuarSinergia(Heroi candidato, List<Heroi> aliados) {
        if (!candidato.podeJogarNaRota(Rota.ROAMING)) {
            return 0;
        }

        return aliados.stream()
            .filter(aliado -> aliado.podeJogarNaRota(Rota.FARM_LANE))
            .map(aliado -> buscarSinergia(candidato.getNome(), aliado.getNome()))
            .flatMap(Optional::stream)
            .mapToInt(sinergia -> (sinergia.nota() - 5) * 2)
            .max()
            .orElse(0);
    }

    public int pontuarRespostaAosInimigos(
        Heroi candidato,
        List<Heroi> inimigos
    ) {
        if (!candidato.podeJogarNaRota(Rota.ROAMING) || inimigos.isEmpty()) {
            return 0;
        }

        Optional<PerfilSuporte> perfil = buscarPerfil(candidato.getNome());
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

        double mobilidade = media(herois, atributo -> atributo.mobilidade());
        double alcance = media(herois, atributo -> atributo.alcance());
        double resistencia = media(herois, atributo -> atributo.resistencia());
        long controlesFortes = herois.stream()
            .filter(heroi -> heroi.getAtributos().controle() >= 7)
            .count();
        long explosivos = herois.stream()
            .filter(heroi -> heroi.getAtributos().danoExplosivo() >= 8)
            .count();
        long imoveis = herois.stream()
            .filter(heroi -> heroi.getAtributos().mobilidade() <= 3)
            .count();

        if (mobilidade >= 7) {
            tipos.add(TipoComposicao.ALTA_MOBILIDADE);
        }
        if (alcance >= 7) {
            tipos.add(TipoComposicao.POKE);
        }
        if (resistencia >= 7) {
            tipos.add(TipoComposicao.FRONT_TO_BACK);
        }
        if (controlesFortes >= 2) {
            tipos.add(TipoComposicao.CONTROLE_PESADO);
            tipos.add(TipoComposicao.ENGAGE_AGRUPADO);
        }
        if (explosivos >= 2) {
            tipos.add(TipoComposicao.EXPLOSAO);
        }
        if (imoveis >= 2) {
            tipos.add(TipoComposicao.ALVOS_IMOVEIS);
        }
        if (possuiCaracteristica(herois, "assassino", "execucao", "mergulho")) {
            tipos.add(TipoComposicao.DIVE);
        }
        if (possuiCaracteristica(herois, "cura", "escudo", "sustentacao")) {
            tipos.add(TipoComposicao.ESCUDOS_E_CURA);
            tipos.add(TipoComposicao.LUTAS_LONGAS);
        }
        if (possuiCaracteristica(herois, "pickoff", "gancho", "supressao")) {
            tipos.add(TipoComposicao.PICKOFF);
        }

        return tipos;
    }

    private SuporteDetalhadoResponse detalhar(Heroi heroi, int limiteDuplas) {
        PerfilSuporte perfil = buscarPerfil(heroi.getNome())
            .orElseThrow(() -> new IllegalStateException(
                "Perfil estratégico ausente para " + heroi.getNome() + "."
            ));

        List<SinergiaBotLane> melhoresDuplas = sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(sinergia.suporte(), heroi.getNome()))
            .sorted(comparadorSinergia())
            .limit(limiteDuplas)
            .toList();

        return new SuporteDetalhadoResponse(heroi, perfil, melhoresDuplas);
    }

    private RecomendacaoComposicaoSuporteResponse avaliarContra(
        PerfilSuporte perfil,
        Set<TipoComposicao> inimigos
    ) {
        List<TipoComposicao> respostas = intersecao(perfil.quebra(), inimigos);
        List<TipoComposicao> riscos = intersecao(perfil.sofreContra(), inimigos);
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
            motivos.add("Não possui counter direto cadastrado para os tipos informados.");
        }

        return new RecomendacaoComposicaoSuporteResponse(
            perfil.heroi(),
            pontuacao,
            respostas,
            riscos,
            motivos
        );
    }

    private Heroi resolverSuporte(String nome) {
        return catalogoSuporte.buscarPorNome(nome)
            .orElseThrow(() -> new RegraNegocioException(
                "Suporte não encontrado: " + nome + "."
            ));
    }

    private Heroi resolverAtirador(String nome) {
        Heroi heroi = heroiService.buscarPorNome(nome)
            .orElseThrow(() -> new RegraNegocioException(
                "Herói não encontrado: " + nome + "."
            ));

        if (!heroi.podeJogarNaRota(Rota.FARM_LANE)) {
            throw new RegraNegocioException(
                heroi.getNome() + " não está cadastrado para a Farm Lane."
            );
        }

        return heroi;
    }

    private Optional<PerfilSuporte> buscarPerfil(String nome) {
        return perfilRepository.listarTodos()
            .stream()
            .filter(perfil -> nomesIguais(perfil.heroi(), nome))
            .findFirst();
    }

    private Optional<SinergiaBotLane> buscarSinergia(
        String suporte,
        String atirador
    ) {
        return sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(sinergia.suporte(), suporte))
            .filter(sinergia -> nomesIguais(sinergia.atirador(), atirador))
            .findFirst();
    }

    private Comparator<SinergiaBotLane> comparadorSinergia() {
        return Comparator
            .comparingInt(SinergiaBotLane::nota)
            .reversed()
            .thenComparing(
                Comparator.comparingInt(
                    sinergia -> pesoConfianca(sinergia.confianca())
                ).reversed()
            )
            .thenComparing(SinergiaBotLane::suporte)
            .thenComparing(SinergiaBotLane::atirador);
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
            .anyMatch(caracteristica -> termos.stream().anyMatch(caracteristica::contains));
    }

    private double media(
        List<Heroi> herois,
        java.util.function.ToIntFunction<AtributosHeroi> extrator
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
