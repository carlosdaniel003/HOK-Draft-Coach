package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;

@Service
@Primary
public class HeroiServiceCompleto extends HeroiService {

    private final List<Heroi> herois;

    @Autowired
    public HeroiServiceCompleto(
        CatalogoSuporteRepository catalogoSuporte,
        CatalogoMidRepository catalogoMid,
        CatalogoJungleRepository catalogoJungle,
        CatalogoClashRepository catalogoClash
    ) {
        Map<String, Heroi> porNome = catalogoBase();
        adicionar(porNome, catalogoSuporte.listarTodos());
        adicionar(porNome, catalogoMid.listarTodos());
        adicionar(porNome, catalogoJungle.listarTodos());
        adicionar(porNome, catalogoClash.listarTodos());
        this.herois = List.copyOf(porNome.values());
    }

    public HeroiServiceCompleto(
        CatalogoSuporteRepository catalogoSuporte,
        CatalogoMidRepository catalogoMid,
        CatalogoJungleRepository catalogoJungle
    ) {
        Map<String, Heroi> porNome = catalogoBase();
        adicionar(porNome, catalogoSuporte.listarTodos());
        adicionar(porNome, catalogoMid.listarTodos());
        adicionar(porNome, catalogoJungle.listarTodos());
        this.herois = List.copyOf(porNome.values());
    }

    public HeroiServiceCompleto(
        CatalogoSuporteRepository catalogoSuporte,
        CatalogoMidRepository catalogoMid
    ) {
        Map<String, Heroi> porNome = catalogoBase();
        adicionar(porNome, catalogoSuporte.listarTodos());
        adicionar(porNome, catalogoMid.listarTodos());
        this.herois = List.copyOf(porNome.values());
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
        String nomeNormalizado = normalizar(nome);

        Optional<Heroi> nomeCanonico = herois.stream()
            .filter(heroi -> normalizar(heroi.getNome()).equals(nomeNormalizado))
            .findFirst();

        return nomeCanonico.isPresent()
            ? nomeCanonico
            : herois.stream()
                .filter(heroi -> heroi.correspondeAoNome(nome))
                .findFirst();
    }

    private Map<String, Heroi> catalogoBase() {
        Map<String, Heroi> porNome = new LinkedHashMap<>();
        adicionar(porNome, super.listarTodos());
        return porNome;
    }

    private void adicionar(Map<String, Heroi> destino, List<Heroi> origem) {
        origem.forEach(heroi -> destino.put(normalizar(heroi.getNome()), heroi));
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
}
