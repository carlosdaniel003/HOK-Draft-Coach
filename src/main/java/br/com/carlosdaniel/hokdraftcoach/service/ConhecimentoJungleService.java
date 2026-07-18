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

import br.com.carlosdaniel.hokdraftcoach.dto.JungleDetalhadoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoJungleResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoEncaixeJungleResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.ConfiancaDado;
import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoJungle;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilJungle;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaJungleEquipe;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.ConfrontoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.PerfilJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaJungleRepository;

@Service
public class ConhecimentoJungleService {

    private final HeroiService heroiService;
    private final CatalogoJungleRepository catalogoJungle;
    private final PerfilJungleRepository perfilRepository;
    private final ConfrontoJungleRepository confrontoRepository;
    private final SinergiaJungleRepository sinergiaRepository;

    public ConhecimentoJungleService(
        HeroiService heroiService,
        CatalogoJungleRepository catalogoJungle,
        PerfilJungleRepository perfilRepository,
        ConfrontoJungleRepository confrontoRepository,
        SinergiaJungleRepository sinergiaRepository
    ) {
        this.heroiService = heroiService;
        this.catalogoJungle = catalogoJungle;
        this.perfilRepository = perfilRepository;
        this.confrontoRepository = confrontoRepository;
        this.sinergiaRepository = sinergiaRepository;
    }

    public List<JungleDetalhadoResponse> listarJunglers() {
        return catalogoJungle.listarTodos()
            .stream()
            .map(heroi -> detalhar(heroi, 5))
            .sorted(Comparator.comparing(
                resposta -> resposta.heroi().getNome()
            ))
            .toList();
    }

    public JungleDetalhadoResponse buscarJungler(String nome) {
        return detalhar(resolverJungler(nome), 10);
    }

    public List<ConfrontoJungle> melhoresRespostasParaJungler(
        String nomeInimigo,
        int limite
    ) {
        Heroi inimigo = resolverJungler(nomeInimigo);
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

    public ConfrontoJungle analisarConfronto(
        String nomeCandidato,
        String nomeInimigo
    ) {
        Heroi candidato = resolverJungler(nomeCandidato);
        Heroi inimigo = resolverJungler(nomeInimigo);

        return buscarConfronto(candidato.getNome(), inimigo.getNome())
            .orElseThrow(() -> new RegraNegocioException(
                "O confronto " + candidato.getNome() + " contra "
                    + inimigo.getNome() + " ainda não possui avaliação curada."
            ));
    }

    public List<SinergiaJungleEquipe> melhoresCombosDoJungler(
        String nomeJungler,
        int limite
    ) {
        Heroi jungler = resolverJungler(nomeJungler);
        validarLimite(limite);

        return sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(
                sinergia.jungler(),
                jungler.getNome()
            ))
            .sorted(comparadorSinergia())
            .limit(limite)
            .toList();
    }

    public List<SinergiaJungleEquipe> melhoresJunglersParaAliado(
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

    public SinergiaJungleEquipe analisarCombo(
        String nomeJungler,
        String nomeAliado
    ) {
        Heroi jungler = resolverJungler(nomeJungler);
        Heroi aliado = resolverHeroi(nomeAliado);

        return buscarSinergia(jungler.getNome(), aliado.getNome())
            .orElseThrow(() -> new RegraNegocioException(
                "O combo " + jungler.getNome() + " + " + aliado.getNome()
                    + " ainda não possui avaliação curada."
            ));
    }

    public List<RecomendacaoComposicaoJungleResponse> recomendarContraComposicao(
        List<TipoComposicao> composicaoInimiga,
        int limite
    ) {
        validarTipos(composicaoInimiga);
        validarLimite(limite);

        Set<TipoComposicao> tipos = new LinkedHashSet<>(composicaoInimiga);

        return perfilRepository.listarTodos()
            .stream()
            .map(perfil -> avaliarContra(perfil, tipos))
            .sorted(
                Comparator
                    .comparingInt(
                        RecomendacaoComposicaoJungleResponse::pontuacao
                    )
                    .reversed()
                    .thenComparing(
                        RecomendacaoComposicaoJungleResponse::jungler
                    )
            )
            .limit(limite)
            .toList();
    }

    public List<RecomendacaoEncaixeJungleResponse> recomendarParaComposicaoAliada(
        List<TipoComposicao> composicaoAliada,
        int limite
    ) {
        validarTipos(composicaoAliada);
        validarLimite(limite);

        Set<TipoComposicao> tipos = new LinkedHashSet<>(composicaoAliada);

        return perfilRepository.listarTodos()
            .stream()
            .map(perfil -> avaliarEncaixe(perfil, tipos))
            .sorted(
                Comparator
                    .comparingInt(
                        RecomendacaoEncaixeJungleResponse::pontuacao
                    )
                    .reversed()
                    .thenComparing(
                        RecomendacaoEncaixeJungleResponse::jungler
                    )
            )
            .limit(limite)
            .toList();
    }

    public int pontuarConfronto(Heroi candidato, Heroi adversario) {
        if (
            candidato == null
                || adversario == null
                || !candidato.podeJogarNaRota(Rota.JUNGLE)
                || !adversario.podeJogarNaRota(Rota.JUNGLE)
        ) {
            return 0;
        }

        Optional<ConfrontoJungle> favoravel = buscarConfronto(
            candidato.getNome(),
            adversario.getNome()
        );
        if (favoravel.isPresent()) {
            return (favoravel.get().vantagem() - 5) * 2;
        }

        Optional<ConfrontoJungle> desfavoravel = buscarConfronto(
            adversario.getNome(),
            candidato.getNome()
        );
        return desfavoravel
            .map(confronto -> -((confronto.vantagem() - 5) * 2))
            .orElse(0);
    }

    public int pontuarSinergia(Heroi candidato, List<Heroi> aliados) {
        if (
            candidato == null
                || !candidato.podeJogarNaRota(Rota.JUNGLE)
                || aliados == null
                || aliados.isEmpty()
        ) {
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
        if (
            candidato == null
                || !candidato.podeJogarNaRota(Rota.JUNGLE)
                || inimigos == null
                || inimigos.isEmpty()
        ) {
            return 0;
        }

        Optional<PerfilJungle> perfil = buscarPerfil(candidato.getNome());
        if (perfil.isEmpty()) {
            return 0;
        }

        Set<TipoComposicao> tipos = inferirComposicao(inimigos);
        int positivos = intersecao(perfil.get().quebra(), tipos).size();
        int negativos = intersecao(perfil.get().sofreContra(), tipos).size();

        return limitar(positivos * 4 - negativos * 3, -12, 12);
    }

    public int pontuarEncaixeAliado(
        Heroi candidato,
        List<Heroi> aliados
    ) {
        if (
            candidato == null
                || !candidato.podeJogarNaRota(Rota.JUNGLE)
                || aliados == null
                || aliados.isEmpty()
        ) {
            return 0;
        }

        Optional<PerfilJungle> perfil = buscarPerfil(candidato.getNome());
        if (perfil.isEmpty()) {
            return 0;
        }

        Set<TipoComposicao> tipos = inferirComposicao(aliados);
        int encaixes = intersecao(perfil.get().fortalece(), tipos).size();
        int bonusEstrutural = calcularBonusEstrutural(perfil.get(), aliados);

        return limitar(encaixes * 3 + bonusEstrutural, 0, 12);
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
            tipos.add(TipoComposicao.ROTACAO);
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
            tipos.add(TipoComposicao.PICKOFF);
        }
        if (possuiCaracteristica(
            herois,
            "cura",
            "escudo",
            "sustentacao",
            "roubo de vida"
        )) {
            tipos.add(TipoComposicao.ESCUDOS_E_CURA);
        }
        if (possuiCaracteristica(
            herois,
            "invasao",
            "roubo de campos"
        )) {
            tipos.add(TipoComposicao.INVASAO);
        }
        if (possuiCaracteristica(
            herois,
            "objetivos",
            "controle de objetivos"
        )) {
            tipos.add(TipoComposicao.OBJETIVOS);
        }
        if (possuiCaracteristica(
            herois,
            "snowball",
            "inicio de jogo",
            "gank"
        )) {
            tipos.add(TipoComposicao.SNOWBALL);
        }
        if (possuiCaracteristica(
            herois,
            "anti-tanque",
            "dano verdadeiro",
            "dano percentual"
        )) {
            tipos.add(TipoComposicao.ANTI_TANQUE);
        }
        if (possuiCaracteristica(
            herois,
            "split push",
            "pressao lateral"
        )) {
            tipos.add(TipoComposicao.SPLIT_PUSH);
        }
        if (possuiCaracteristica(
            herois,
            "fim de jogo",
            "escalamento"
        )) {
            tipos.add(TipoComposicao.ESCALAMENTO);
        }
        if (possuiCaracteristica(
            herois,
            "dano magico",
            "mago jungle",
            "assassino magico"
        )) {
            tipos.add(TipoComposicao.ASSASSINO_MAGICO);
        }

        return tipos;
    }

    private JungleDetalhadoResponse detalhar(Heroi heroi, int limite) {
        PerfilJungle perfil = buscarPerfil(heroi.getNome())
            .orElseThrow(() -> new IllegalStateException(
                "Perfil estratégico ausente para " + heroi.getNome() + "."
            ));

        List<ConfrontoJungle> vantagens = confrontoRepository.listarTodos()
            .stream()
            .filter(confronto -> nomesIguais(
                confronto.vencedor(),
                heroi.getNome()
            ))
            .sorted(comparadorConfronto())
            .limit(limite)
            .toList();

        List<ConfrontoJungle> desvantagens = confrontoRepository.listarTodos()
            .stream()
            .filter(confronto -> nomesIguais(
                confronto.alvo(),
                heroi.getNome()
            ))
            .sorted(comparadorConfronto())
            .limit(limite)
            .toList();

        List<SinergiaJungleEquipe> combos = sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(
                sinergia.jungler(),
                heroi.getNome()
            ))
            .sorted(comparadorSinergia())
            .limit(limite)
            .toList();

        return new JungleDetalhadoResponse(
            heroi,
            perfil,
            vantagens,
            desvantagens,
            combos
        );
    }

    private RecomendacaoComposicaoJungleResponse avaliarContra(
        PerfilJungle perfil,
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
            45
                + respostas.size() * 14
                - riscos.size() * 10
                + perfil.seguranca() / 2,
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

        return new RecomendacaoComposicaoJungleResponse(
            perfil.heroi(),
            pontuacao,
            respostas,
            riscos,
            motivos
        );
    }

    private RecomendacaoEncaixeJungleResponse avaliarEncaixe(
        PerfilJungle perfil,
        Set<TipoComposicao> aliados
    ) {
        List<TipoComposicao> encaixes = intersecao(
            perfil.fortalece(),
            aliados
        );
        int pontuacao = limitar(
            45
                + encaixes.size() * 12
                + perfil.objetivos() / 2
                + perfil.seguranca() / 2,
            0,
            100
        );
        List<String> motivos = new ArrayList<>();

        if (!encaixes.isEmpty()) {
            motivos.add("Fortalece o plano aliado: " + encaixes + ".");
        } else {
            motivos.add(
                "Encaixe neutro: não reforça diretamente os tipos informados."
            );
        }
        if (perfil.linhaDeFrente() >= 8) {
            motivos.add("Acrescenta linha de frente à composição.");
        }
        if (perfil.objetivos() >= 9) {
            motivos.add("Acrescenta forte controle de objetivos.");
        }
        if (perfil.gank() >= 9) {
            motivos.add("Acrescenta alta pressão de gank e criação de vantagem.");
        }

        return new RecomendacaoEncaixeJungleResponse(
            perfil.heroi(),
            pontuacao,
            encaixes,
            motivos
        );
    }

    private int calcularBonusEstrutural(
        PerfilJungle perfil,
        List<Heroi> aliados
    ) {
        boolean possuiLinhaDeFrente = aliados.stream()
            .anyMatch(aliado -> aliado.getAtributos().resistencia() >= 8);
        boolean possuiControle = aliados.stream()
            .anyMatch(aliado -> aliado.getAtributos().controle() >= 8);
        boolean possuiDanoSustentado = aliados.stream()
            .anyMatch(aliado -> aliado.getAtributos().danoSustentado() >= 8);

        int bonus = 0;
        if (!possuiLinhaDeFrente && perfil.linhaDeFrente() >= 8) {
            bonus += 4;
        }
        if (!possuiControle && perfil.gank() >= 8) {
            bonus += 3;
        }
        if (!possuiDanoSustentado && perfil.danoSustentado() >= 8) {
            bonus += 3;
        }
        if (perfil.objetivos() >= 9) {
            bonus += 2;
        }
        return bonus;
    }

    private Heroi resolverJungler(String nome) {
        return catalogoJungle.buscarPorNome(nome)
            .orElseThrow(() -> new RegraNegocioException(
                "Jungler não encontrado: " + nome + "."
            ));
    }

    private Heroi resolverHeroi(String nome) {
        return heroiService.buscarPorNome(nome)
            .orElseThrow(() -> new RegraNegocioException(
                "Herói não encontrado: " + nome + "."
            ));
    }

    private Optional<PerfilJungle> buscarPerfil(String nome) {
        return perfilRepository.listarTodos()
            .stream()
            .filter(perfil -> nomesIguais(perfil.heroi(), nome))
            .findFirst();
    }

    private Optional<ConfrontoJungle> buscarConfronto(
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

    private Optional<SinergiaJungleEquipe> buscarSinergia(
        String jungler,
        String aliado
    ) {
        return sinergiaRepository.listarTodas()
            .stream()
            .filter(sinergia -> nomesIguais(
                sinergia.jungler(),
                jungler
            ))
            .filter(sinergia -> nomesIguais(sinergia.aliado(), aliado))
            .findFirst();
    }

    private Comparator<ConfrontoJungle> comparadorConfronto() {
        Comparator<ConfrontoJungle> porConfianca = Comparator
            .comparingInt(
                (ConfrontoJungle confronto) -> pesoConfianca(
                    confronto.confianca()
                )
            )
            .reversed();

        return Comparator
            .comparingInt(ConfrontoJungle::vantagem)
            .reversed()
            .thenComparing(porConfianca)
            .thenComparing(ConfrontoJungle::vencedor)
            .thenComparing(ConfrontoJungle::alvo);
    }

    private Comparator<SinergiaJungleEquipe> comparadorSinergia() {
        Comparator<SinergiaJungleEquipe> porConfianca = Comparator
            .comparingInt(
                (SinergiaJungleEquipe sinergia) -> pesoConfianca(
                    sinergia.confianca()
                )
            )
            .reversed();

        return Comparator
            .comparingInt(SinergiaJungleEquipe::nota)
            .reversed()
            .thenComparing(porConfianca)
            .thenComparing(SinergiaJungleEquipe::jungler)
            .thenComparing(SinergiaJungleEquipe::aliado);
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

    private void validarTipos(List<TipoComposicao> tipos) {
        if (tipos == null || tipos.isEmpty()) {
            throw new RegraNegocioException(
                "Informe ao menos um arquétipo de composição."
            );
        }
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
