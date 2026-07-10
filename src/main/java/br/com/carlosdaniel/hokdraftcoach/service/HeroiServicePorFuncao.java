package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
@Primary
public class HeroiServicePorFuncao extends HeroiService {

    private static final ThreadLocal<List<Rota>> FUNCOES_RESTRITAS =
        ThreadLocal.withInitial(List::of);

    static EscopoFuncao restringirA(List<Rota> funcoes) {
        List<Rota> anteriores = FUNCOES_RESTRITAS.get();
        FUNCOES_RESTRITAS.set(List.copyOf(funcoes));
        return new EscopoFuncao(anteriores);
    }

    @Override
    public List<Heroi> listarTodos() {
        List<Rota> funcoes = FUNCOES_RESTRITAS.get();
        if (funcoes.isEmpty()) {
            return super.listarTodos();
        }

        return super.listarTodos().stream()
            .map(heroi -> limitarAsFuncoes(heroi, funcoes))
            .filter(heroi -> heroi != null)
            .toList();
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
