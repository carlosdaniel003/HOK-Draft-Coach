package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.FuncaoSlotRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
@Primary
public class RecomendacaoProximoPickPorFuncaoService
    extends RecomendacaoProximoPickService {

    public RecomendacaoProximoPickPorFuncaoService(
        HeroiServicePorFuncao heroiService,
        InferenciaFuncoesService inferenciaFuncoesService
    ) {
        super(heroiService, inferenciaFuncoesService);
    }

    @Override
    public RecomendacaoProximoPickResponse recomendar(
        RecomendacaoProximoPickRequest request
    ) {
        validarFuncoesInformadas(request);
        Integer ordemAlvo = proximaOrdemAliada(request);
        List<Rota> funcoesAlvo = request.funcoesDaOrdem(ordemAlvo);
        if (funcoesAlvo.isEmpty()) {
            return super.recomendar(request);
        }

        try (
            HeroiServicePorFuncao.EscopoFuncao ignored =
                HeroiServicePorFuncao.restringirA(funcoesAlvo)
        ) {
            return super.recomendar(request);
        }
    }

    private void validarFuncoesInformadas(
        RecomendacaoProximoPickRequest request
    ) {
        Set<Integer> ordens = new HashSet<>();
        Set<Rota> funcoes = new HashSet<>();

        for (FuncaoSlotRequest item : request.funcoesAliadas()) {
            if (item == null || item.ordem() == null || item.funcao() == null) {
                throw new RegraNegocioException(
                    "Toda função aliada deve informar ordem e função."
                );
            }
            if (!ordens.add(item.ordem())) {
                throw new RegraNegocioException(
                    "A função do jogador " + item.ordem()
                        + " foi informada mais de uma vez."
                );
            }
            if (
                request.minhaOrdem() != null
                    && request.minhaOrdem().equals(item.ordem())
            ) {
                throw new RegraNegocioException(
                    "A sua função deve ser informada no campo Minha função."
                );
            }
            if (!funcoes.add(item.funcao())) {
                throw new RegraNegocioException(
                    "A função " + item.funcao()
                        + " foi atribuída a mais de um jogador aliado."
                );
            }
        }

        if (request.funcoesPreferidas().size() == 1) {
            Rota minhaFuncao = request.funcoesPreferidas().getFirst();
            if (!funcoes.add(minhaFuncao)) {
                throw new RegraNegocioException(
                    "A função " + minhaFuncao
                        + " já foi atribuída a outro jogador aliado."
                );
            }
        }
    }

    private Integer proximaOrdemAliada(
        RecomendacaoProximoPickRequest request
    ) {
        if (request.meuLado() == null) {
            return null;
        }
        List<PickSemFuncaoRequest> aliados =
            request.meuLado() == LadoDraft.AZUL
                ? request.picksAzul()
                : request.picksVermelho();

        for (int ordem = 1; ordem <= 5; ordem += 1) {
            int ordemAtual = ordem;
            boolean preenchido = aliados.stream()
                .anyMatch(pick -> pick.ordem().equals(ordemAtual));
            if (!preenchido) {
                return ordem;
            }
        }
        return null;
    }
}
