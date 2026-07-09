package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;

@Service
@Primary
public class HeroiServiceCompleto extends HeroiService {

    private final List<Heroi> herois;

    public HeroiServiceCompleto(
        CatalogoSuporteRepository catalogoSuporte,
        CatalogoMidRepository catalogoMid,
        CatalogoJungleRepository catalogoJungle
    ) {
        Map<Long, Heroi> porId = new LinkedHashMap<>();
        super.listarTodos().forEach(heroi -> porId.put(heroi.getId(), heroi));
        catalogoSuporte.listarTodos().forEach(
            heroi -> porId.put(heroi.getId(), heroi)
        );
        catalogoMid.listarTodos().forEach(
            heroi -> porId.put(heroi.getId(), heroi)
        );
        catalogoJungle.listarTodos().forEach(
            heroi -> porId.put(heroi.getId(), heroi)
        );
        this.herois = List.copyOf(porId.values());
    }

    @Override
    public List<Heroi> listarTodos() {
        return herois;
    }

    @Override
    public List<Heroi> listarPorRota(Rota rota) {
        return herois.stream()
            .filter(heroi -> heroi.podeJogarNaRota(rota))
            .toList();
    }

    @Override
    public Optional<Heroi> buscarPorId(Long id) {
        return herois.stream()
            .filter(heroi -> heroi.getId().equals(id))
            .findFirst();
    }

    @Override
    public Optional<Heroi> buscarPorNome(String nome) {
        return herois.stream()
            .filter(heroi -> heroi.correspondeAoNome(nome))
            .findFirst();
    }
}
