package br.com.carlosdaniel.hokdraftcoach.model;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class Heroi {

    private final Long id;
    private final String nome;
    private final List<String> aliases;
    private final ClasseHeroi classe;
    private final Rota rota;
    private final List<Rota> rotasPossiveis;
    private final String estilo;
    private final int dificuldade;
    private final TipoDano tipoDano;
    private final AtributosHeroi atributos;
    private final List<String> caracteristicas;
    private final DadosMetaHeroi dadosMeta;

    public Heroi(
        Long id,
        String nome,
        Rota rota,
        String estilo,
        int dificuldade,
        TipoDano tipoDano,
        AtributosHeroi atributos,
        List<String> caracteristicas
    ) {
        this(
            id,
            nome,
            List.of(),
            ClasseHeroi.HIBRIDO,
            rota,
            List.of(rota),
            estilo,
            dificuldade,
            tipoDano,
            atributos,
            caracteristicas,
            DadosMetaHeroi.naoClassificado()
        );
    }

    public Heroi(
        Long id,
        String nome,
        Rota rota,
        List<Rota> rotasPossiveis,
        String estilo,
        int dificuldade,
        TipoDano tipoDano,
        AtributosHeroi atributos,
        List<String> caracteristicas
    ) {
        this(
            id,
            nome,
            List.of(),
            ClasseHeroi.HIBRIDO,
            rota,
            rotasPossiveis,
            estilo,
            dificuldade,
            tipoDano,
            atributos,
            caracteristicas,
            DadosMetaHeroi.naoClassificado()
        );
    }

    public Heroi(
        Long id,
        String nome,
        List<String> aliases,
        ClasseHeroi classe,
        Rota rota,
        List<Rota> rotasPossiveis,
        String estilo,
        int dificuldade,
        TipoDano tipoDano,
        AtributosHeroi atributos,
        List<String> caracteristicas,
        DadosMetaHeroi dadosMeta
    ) {
        this.id = id;
        this.nome = nome;
        this.aliases = List.copyOf(aliases);
        this.classe = classe;
        this.rota = rota;
        this.rotasPossiveis = normalizarRotas(rota, rotasPossiveis);
        this.estilo = estilo;
        this.dificuldade = dificuldade;
        this.tipoDano = tipoDano;
        this.atributos = atributos;
        this.caracteristicas = List.copyOf(caracteristicas);
        this.dadosMeta = dadosMeta;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public ClasseHeroi getClasse() {
        return classe;
    }

    public Rota getRota() {
        return rota;
    }

    public List<Rota> getRotasPossiveis() {
        return rotasPossiveis;
    }

    public boolean podeJogarNaRota(Rota rotaConsultada) {
        return rotasPossiveis.contains(rotaConsultada);
    }

    public boolean isFlex() {
        return rotasPossiveis.size() > 1;
    }

    public boolean correspondeAoNome(String nomeConsultado) {
        String normalizado = normalizarNome(nomeConsultado);

        if (normalizarNome(nome).equals(normalizado)) {
            return true;
        }

        return aliases.stream()
            .map(this::normalizarNome)
            .anyMatch(normalizado::equals);
    }

    public String getEstilo() {
        return estilo;
    }

    public int getDificuldade() {
        return dificuldade;
    }

    public TipoDano getTipoDano() {
        return tipoDano;
    }

    public AtributosHeroi getAtributos() {
        return atributos;
    }

    public List<String> getCaracteristicas() {
        return caracteristicas;
    }

    public DadosMetaHeroi getDadosMeta() {
        return dadosMeta;
    }

    private List<Rota> normalizarRotas(
        Rota rotaPrincipal,
        List<Rota> rotasInformadas
    ) {
        LinkedHashSet<Rota> rotas = new LinkedHashSet<>();
        rotas.add(rotaPrincipal);
        rotas.addAll(rotasInformadas);
        return List.copyOf(rotas);
    }

    private String normalizarNome(String valor) {
        if (valor == null) {
            return "";
        }

        return Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase(Locale.ROOT);
    }
}
