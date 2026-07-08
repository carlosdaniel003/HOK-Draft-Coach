package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.LinkedHashSet;
import java.util.List;

public class Heroi {

    private final Long id;
    private final String nome;
    private final Rota rota;
    private final List<Rota> rotasPossiveis;
    private final String estilo;
    private final int dificuldade;
    private final TipoDano tipoDano;
    private final AtributosHeroi atributos;
    private final List<String> caracteristicas;

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
            rota,
            List.of(rota),
            estilo,
            dificuldade,
            tipoDano,
            atributos,
            caracteristicas
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
        this.id = id;
        this.nome = nome;
        this.rota = rota;
        this.rotasPossiveis = normalizarRotas(rota, rotasPossiveis);
        this.estilo = estilo;
        this.dificuldade = dificuldade;
        this.tipoDano = tipoDano;
        this.atributos = atributos;
        this.caracteristicas = List.copyOf(caracteristicas);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
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

    private List<Rota> normalizarRotas(
        Rota rotaPrincipal,
        List<Rota> rotasInformadas
    ) {
        LinkedHashSet<Rota> rotas = new LinkedHashSet<>();
        rotas.add(rotaPrincipal);
        rotas.addAll(rotasInformadas);
        return List.copyOf(rotas);
    }
}
