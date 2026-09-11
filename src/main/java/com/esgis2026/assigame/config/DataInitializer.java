package com.esgis2026.assigame.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.entity.CategorieProduit;
import com.esgis2026.assigame.entity.TypeUtilisateur;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.repository.CategorieProduitRepository;
import com.esgis2026.assigame.repository.CommandeRepository;
import com.esgis2026.assigame.repository.ConversationRepository;
import com.esgis2026.assigame.repository.EnchereOffreRepository;
import com.esgis2026.assigame.repository.EnchereRepository;
import com.esgis2026.assigame.repository.GroupeLivraisonRepository;
import com.esgis2026.assigame.repository.MessageRepository;
import com.esgis2026.assigame.repository.ProduitRepository;
import com.esgis2026.assigame.repository.TypeUtilisateurRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Initialisation v1 : rôles, catégories de base, compte admin uniquement.
 * Aucun produit / client / vendeur / commande de démo.
 * Les images carousel (static/images/commerce) restent des fichiers statiques.
 */
@Configuration
public class DataInitializer {

    private static final List<String> DEMO_EMAILS = List.of(
            "vendeur@assigame.tg",
            "kofi@email.com",
            "admin@togoshop.tg"
    );

    @Bean
    CommandLineRunner seedData(SeedService seedService,
            @Value("${app.seed.purge-demo:true}") boolean purgeDemo) {
        return args -> {
            try {
                seedService.run(purgeDemo);
            } catch (Exception e) {
                System.err.println("[DataInitializer] ÉCHEC : " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    @Component
    public static class SeedService {

        @PersistenceContext
        private EntityManager entityManager;

        private final TypeUtilisateurRepository typeRepo;
        private final UtisateurRepository userRepo;
        private final CategorieProduitRepository categorieRepo;
        private final ProduitRepository produitRepo;
        private final CommandeRepository commandeRepo;
        private final MessageRepository messageRepo;
        private final ConversationRepository conversationRepo;
        private final EnchereOffreRepository enchereOffreRepo;
        private final EnchereRepository enchereRepo;
        private final GroupeLivraisonRepository groupeRepo;
        private final PasswordEncoder passwordEncoder;

        public SeedService(
                TypeUtilisateurRepository typeRepo,
                UtisateurRepository userRepo,
                CategorieProduitRepository categorieRepo,
                ProduitRepository produitRepo,
                CommandeRepository commandeRepo,
                MessageRepository messageRepo,
                ConversationRepository conversationRepo,
                EnchereOffreRepository enchereOffreRepo,
                EnchereRepository enchereRepo,
                GroupeLivraisonRepository groupeRepo,
                PasswordEncoder passwordEncoder) {
            this.typeRepo = typeRepo;
            this.userRepo = userRepo;
            this.categorieRepo = categorieRepo;
            this.produitRepo = produitRepo;
            this.commandeRepo = commandeRepo;
            this.messageRepo = messageRepo;
            this.conversationRepo = conversationRepo;
            this.enchereOffreRepo = enchereOffreRepo;
            this.enchereRepo = enchereRepo;
            this.groupeRepo = groupeRepo;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public void run(boolean purgeDemo) {
            TypeUtilisateur adminType = ensureRole("ADMIN", "Administrateur");
            ensureRole("CLIENT", "Client");
            ensureRole("VENDEUR", "Vendeur");
            ensureRole("LIVREUR", "Livreur");

            if (purgeDemo) {
                purgeDemoData();
            }

            ensureAdmin(adminType);

            ensureCategorie("Électronique", "Téléphones, accessoires, high-tech");
            ensureCategorie("Vêtements", "Mode et textile");
            ensureCategorie("Maison", "Équipement et déco");
            ensureCategorie("Sport", "Sport et loisirs");
            ensureCategorie("Accessoires", "Sacs, montres, bijoux");

            System.out.println("========================================================");
            System.out.println("[DataInitializer] Base prête (v1 — sans données de démo)");
            System.out.println("  ADMIN : admin@assigame.tg  /  admin");
            System.out.println("  Utilisateurs : " + userRepo.count());
            System.out.println("  Produits     : " + produitRepo.count());
            if (purgeDemo) {
                System.out.println("  Tip : mettez app.seed.purge-demo=false après le 1er démarrage propre.");
            }
            System.out.println("========================================================");
        }

        private void purgeDemoData() {
            /* Uniquement du SQL natif dans le bon ordre FK — pas de deleteAllInBatch
               (Hibernate ignore les cascades orphan et casse sur ligne_commande). */
            safeNative("DELETE FROM enchere_offre");
            safeNative("DELETE FROM enchere");
            safeNative("DELETE FROM message");
            safeNative("DELETE FROM conversation");
            safeNative("DELETE FROM groupe_livraison_membre");
            safeNative("DELETE FROM groupe_livraison");
            safeNative("DELETE FROM ligne_commande");
            safeNative("DELETE FROM commande");
            safeNative("DELETE FROM produit_image");
            safeNative("DELETE FROM produit");
            safeNative("UPDATE utilisateur SET id_vendeur_proprietaire = NULL");

            for (String email : DEMO_EMAILS) {
                userRepo.findByEmail(email).ifPresent(userRepo::delete);
            }

            userRepo.findAll().stream()
                    .filter(u -> u.getTypeUtilisateur() == null
                            || !"ADMIN".equalsIgnoreCase(u.getTypeUtilisateur().getNom_utilisateur()))
                    .filter(u -> !"admin@assigame.tg".equalsIgnoreCase(u.getEmail()))
                    .forEach(userRepo::delete);

            entityManager.flush();
            entityManager.clear();
            System.out.println("[DataInitializer] Données de démo purgées.");
        }

        private void safeNative(String sql) {
            try {
                entityManager.createNativeQuery(sql).executeUpdate();
            } catch (Exception e) {
                System.out.println("[DataInitializer] Skip SQL (« " + sql + " ») : " + e.getMessage());
            }
        }

        private TypeUtilisateur ensureRole(String role, String desc) {
            return typeRepo.findByRoleName(role).orElseGet(() -> {
                TypeUtilisateur t = new TypeUtilisateur();
                t.setNom_utilisateur(role);
                t.setDescription(desc);
                return typeRepo.save(t);
            });
        }

        private void ensureAdmin(TypeUtilisateur adminType) {
            if (userRepo.findByEmail("admin@assigame.tg").isPresent()) {
                return;
            }
            Utilisateur admin = new Utilisateur();
            admin.setNom("Système");
            admin.setPrenom("Admin");
            admin.setEmail("admin@assigame.tg");
            admin.setTelephone("+228 90 00 00 00");
            admin.setMotdepasse(passwordEncoder.encode("admin"));
            admin.setDate_creation(LocalDateTime.now());
            admin.setStatut("ACTIF");
            admin.setRegion("Maritime");
            admin.setTypeUtilisateur(adminType);
            userRepo.save(admin);
        }

        private void ensureCategorie(String nom, String desc) {
            categorieRepo.findByNom(nom).orElseGet(() -> {
                CategorieProduit c = new CategorieProduit();
                c.setNom_categorieproduit(nom);
                c.setDescription(desc);
                return categorieRepo.save(c);
            });
        }
    }
}
