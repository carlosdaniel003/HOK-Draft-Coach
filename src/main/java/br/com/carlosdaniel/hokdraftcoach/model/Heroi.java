package br.com.carlosdaniel.hokdraftcoach.model;

public class Heroi {

    private Long id;
    private String nome;
    private Rota rota;
    private String estilo;
    private int dificuldade;

    public Heroi(
        Long id,
        String nome,
        Rota rota,
        String estilo,
        int dificuldade
    ) {
        this.id = id;
        this.nome = nome;
        this.rota = rota;
        this.estilo = estilo;
        this.dificuldade = dificuldade;
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

    public String getEstilo() {
        return estilo;
    }

    public int getDificuldade() {
        return dificuldade;
    }
}