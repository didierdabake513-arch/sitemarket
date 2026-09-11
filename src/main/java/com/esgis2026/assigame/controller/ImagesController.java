package com.esgis2026.assigame.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sert des listes d'images pilotées uniquement par le contenu d'un dossier
 * côté serveur (src/main/resources/static/images/&lt;dossier&gt;/).
 *
 * Le front n'a JAMAIS de nom de fichier en dur : il appelle
 * GET /api/images/{dossier} et affiche ce qui revient. Pour ajouter une
 * nouvelle image à un carrousel (ex. le fond "commerce" de l'accueil et
 * du catalogue, ou les régions sur la page de connexion), il suffit de
 * déposer le fichier dans le dossier correspondant — aucune autre
 * modification n'est nécessaire.
 *
 * Dossiers actuellement utilisés :
 *   - "commerce" → carrousel héros + fond du bandeau invité (accueil) + fond de l'en-tête catalogue
 *   - "regions"  → fond de la page de connexion (régions du Togo)
 */
@RestController
public class ImagesController {

    private static final List<String> EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    @GetMapping("/api/images/{dossier}")
    public List<String> list(@PathVariable String dossier) {
        // Sécurité : seuls des noms de dossier simples sont acceptés (pas de "..", "/", etc.)
        if (!dossier.matches("[a-zA-Z0-9_-]+")) {
            return List.of();
        }
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            return EXTENSIONS.stream()
                    .flatMap(ext -> {
                        try {
                            Resource[] found = resolver.getResources("classpath:/static/images/" + dossier + "/*." + ext);
                            return Arrays.stream(found);
                        } catch (IOException e) {
                            return java.util.stream.Stream.<Resource>empty();
                        }
                    })
                    .map(Resource::getFilename)
                    .filter(java.util.Objects::nonNull)
                    .sorted(Comparator.naturalOrder())
                    .map(name -> "/images/" + dossier + "/" + name)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }
}
