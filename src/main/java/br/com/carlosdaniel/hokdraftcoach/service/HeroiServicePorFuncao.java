package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
public class HeroiServicePorFuncao extends HeroiService {

    private static final ThreadLocal<List<Rota>> FUNCOES_RESTRITAS =
        ThreadLocal.withInitial(List::of);

    private final HeroiService catalogoCompleto;

    public HeroiServicePorFuncao(
        @Qualifier("heroiServiceCompleto") HeroiService catalogoCompleto
    ) {
        this.catalogoCompleto = catalogoCompleto;
    }

    static EscopoFuncao restringirA(List<Rota> funcoes) {
        List<Rota> anteriores = FUNCOES_RESTRITAS.get();
        FUNCOES_RESTRITAS.set(List.copyOf(funcoes));
        return new EscopoFuncao(anteriores);
    }

    @Override
    public List<Heroi> listarTodos() {
        List<Rota> funcoes = FUNCOES_RESTRITAS.get();
        if (funcoes.isEmpty()) {
            return catalogoCompleto.listarTodos();
        }

        return catalogoCompleto.listarTodos().stream()
            .map(heroi -> limitarAsFuncoes(heroi, funcoes))
            .filter(heroi -> heroi != null)
            .toList();
    }

    @Override
    public List<Heroi> listarPorRota(Rota rota) {
        return listarTodos().stream()
            .filter(heroi -> heroi.podeJogarNaRota(rota))
            .toList();
    }

    @Override
    public Optional<Heroi> buscarPorId(Long id) {
        return catalogoCompleto.buscarPorId(id);
    }

    @Override
    public Optional<Heroi> buscarPorNome(String nome) {
        return catalogoCompleto.buscarPorNome(nome);
    }

    private Heroi limitarAsFuncoes(Heroi heroi, List<Rota> funcoes) {
        List<Rota> rotasPermitidas = heroi.getRotasPossiveis().stream()
            .filter(funcoes::contains)
            .toList();

        if (rotasPermitidas.isEmpty()) {
            return null;
        }

        Rota rotaPrincipal = rotasPermitidas.contains(heroi.getRota())
            ? heroi.getRota()
            : rotasPermitidas.getFirst();

        return new Heroi(
            heroi.getId(),
            heroi.getNome(),
            heroi.getAliases(),
            heroi.getClasse(),
            rotaPrincipal,
            rotasPermitidas,
            heroi.getEstilo(),
            heroi.getDificuldade(),
            heroi.getTipoDano(),
            heroi.getAtributos(),
            heroi.getCaracteristicas(),
            heroi.getDadosMeta()
        );
    }

    static final class EscopoFuncao implements AutoCloseable {

        private final List<Rota> funcoesAnteriores;
        private boolean fechado;

        private EscopoFuncao(List<Rota> funcoesAnteriores) {
            this.funcoesAnteriores = funcoesAnteriores;
        }

        @Override
        public void close() {
            if (fechado) {
                return;
            }
            FUNCOES_RESTRITAS.set(funcoesAnteriores);
            fechado = true;
        }
    }
}
