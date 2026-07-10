package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;

@Service
@Primary
public class RecomendacaoProximoPickPorFuncaoService
    extends RecomendacaoProximoPickService {

    public RecomendacaoProximoPickPorFuncaoService(
        HeroiService heroiService,
        InferenciaFuncoesService inferenciaFuncoesService
    ) {
        super(heroiService, inferenciaFuncoesService);
    }

    @Override
    public RecomendacaoProximoPickResponse recomendar(
        RecomendacaoProximoPickRequest request
    ) {
        if (!recomendacaoEhParaOUsuario(request)) {
            return super.recomendar(request);
        }

        try (
            HeroiServicePorFuncao.EscopoFuncao ignored =
                HeroiServicePorFuncao.restringirA(request.funcoesPreferidas())
        ) {
            return super.recomendar(request);
        }
    }

    private boolean recomendacaoEhParaOUsuario(
        RecomendacaoProximoPickRequest request
    ) {
        if (
            request.meuLado() == null
                || request.minhaOrdem() == null
                || request.funcoesPreferidas().isEmpty()
        ) {
            return false;
        }

        List<PickSemFuncaoRequest> aliados = request.meuLado() == LadoDraft.AZUL
            ? request.picksAzul()
            : request.picksVermelho();

        boolean meuSlotPreenchido = aliados.stream()
            .anyMatch(pick -> pick.ordem().equals(request.minhaOrdem()));
        if (meuSlotPreenchido) {
            return false;
        }

        for (int ordem = 1; ordem <= 5; ordem += 1) {
            int ordemAtual = ordem;
            boolean preenchido = aliados.stream()
                .anyMatch(pick -> pick.ordem().equals(ordemAtual));
            if (!preenchido) {
                return ordemAtual == request.minhaOrdem();
            }
        }

        return false;
    }
}
