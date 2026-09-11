package com.esgis2026.assigame.service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgis2026.assigame.dto.ConversationDto;
import com.esgis2026.assigame.dto.MessageDto;
import com.esgis2026.assigame.entity.Conversation;
import com.esgis2026.assigame.entity.Message;
import com.esgis2026.assigame.entity.Produit;
import com.esgis2026.assigame.entity.Utilisateur;
import com.esgis2026.assigame.repository.ConversationRepository;
import com.esgis2026.assigame.repository.GroupeLivraisonRepository;
import com.esgis2026.assigame.repository.MessageRepository;
import com.esgis2026.assigame.repository.ProduitRepository;
import com.esgis2026.assigame.repository.UtisateurRepository;
import com.esgis2026.assigame.util.SecurityUtils;

/**
 * Messagerie privée et CONFIDENTIELLE (jamais un chiffrement de bout en
 * bout : le contenu est lisible en base par construction, mais n'est jamais
 * exposé par l'application à un tiers). Un vrai chiffrement de bout en bout
 * — où même un accès direct à la base ne permettrait pas de lire les
 * messages — est un chantier cryptographique à part entière (génération et
 * gestion de clés côté client, etc.), noté comme piste pour une prochaine
 * version.
 *
 * Trois types de conversation autorisés, jamais d'autre combinaison :
 *   CLIENT  ↔ VENDEUR  (à propos d'un produit)
 *   VENDEUR ↔ VENDEUR
 *   VENDEUR ↔ LIVREUR  (uniquement si ce livreur est accessible au vendeur —
 *                       le sien, ou celui d'un groupe de livraison rejoint)
 */
@Service
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UtisateurRepository utilisateurRepository;
    private final ProduitRepository produitRepository;
    private final GroupeLivraisonRepository groupeLivraisonRepository;

    public MessageService(ConversationRepository conversationRepository,
                           MessageRepository messageRepository,
                           UtisateurRepository utilisateurRepository,
                           ProduitRepository produitRepository,
                           GroupeLivraisonRepository groupeLivraisonRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.produitRepository = produitRepository;
        this.groupeLivraisonRepository = groupeLivraisonRepository;
    }

    /**
     * Démarre une conversation avec un autre utilisateur, ou récupère celle
     * qui existe déjà entre les deux (jamais de doublon). produitId est
     * optionnel — uniquement pertinent pour CLIENT ↔ VENDEUR.
     */
    @Transactional
    public ConversationDto demarrerOuRecuperer(Long autreUtilisateurId, String autreEmail, Long produitId) {
        Long moiId = SecurityUtils.getCurrentUserId();
        Utilisateur moi = utilisateurRepository.findById(moiId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Utilisateur autre;
        if (autreUtilisateurId != null) {
            autre = utilisateurRepository.findById(autreUtilisateurId)
                    .orElseThrow(() -> new RuntimeException("Destinataire introuvable"));
        } else if (autreEmail != null && !autreEmail.isBlank()) {
            autre = utilisateurRepository.findByEmail(autreEmail.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Aucun compte ne correspond à cet email"));
        } else {
            throw new IllegalArgumentException("Destinataire manquant");
        }

        if (moi.getId().equals(autre.getId())) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous envoyer un message à vous-même");
        }

        String type = determinerType(moi, autre);
        return trouverOuCreerConversation(moi, autre, type, produitId);
    }

    /**
     * Contacte l'administrateur — accessible à n'importe quel compte
     * (CLIENT, VENDEUR, LIVREUR). S'il existe plusieurs comptes ADMIN, on
     * retrouve/utilise systématiquement le même (le premier créé), pour ne
     * jamais éparpiller la conversation entre plusieurs admins.
     */
    @Transactional
    public ConversationDto contacterAdmin() {
        Long moiId = SecurityUtils.getCurrentUserId();
        Utilisateur moi = utilisateurRepository.findById(moiId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if ("ADMIN".equalsIgnoreCase(moi.getTypeUtilisateur().getNom_utilisateur())) {
            throw new IllegalArgumentException("Vous êtes déjà administrateur");
        }
        Utilisateur admin = utilisateurRepository.findByTypeUtilisateur_NomUtilisateurIgnoreCase("ADMIN")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun administrateur disponible pour le moment"));
        return trouverOuCreerConversation(moi, admin, "VERS_ADMIN", null);
    }

    private ConversationDto trouverOuCreerConversation(Utilisateur moi, Utilisateur autre, String type, Long produitId) {
        List<Conversation> existantes = conversationRepository.findEntre(moi.getId(), autre.getId());
        Conversation conv = existantes.isEmpty() ? null : existantes.get(0);
        if (conv == null) {
            conv = new Conversation();
            conv.setType(type);
            conv.setParticipant1(moi);
            conv.setParticipant2(autre);
            if (produitId != null) {
                produitRepository.findById(produitId).ifPresent(conv::setProduitContexte);
            }
            conv = conversationRepository.save(conv);
        }
        return toDto(conv, moi.getId());
    }

    /** Valide la paire de rôles et retourne le type de conversation autorisé — ou refuse. */
    private String determinerType(Utilisateur moi, Utilisateur autre) {
        String roleMoi = moi.getTypeUtilisateur().getNom_utilisateur().toUpperCase();
        String roleAutre = autre.getTypeUtilisateur().getNom_utilisateur().toUpperCase();

        boolean clientVendeur = (roleMoi.equals("CLIENT") && roleAutre.equals("VENDEUR"))
                || (roleMoi.equals("VENDEUR") && roleAutre.equals("CLIENT"));
        if (clientVendeur) return "ACHETEUR_VENDEUR";

        boolean vendeurVendeur = roleMoi.equals("VENDEUR") && roleAutre.equals("VENDEUR");
        if (vendeurVendeur) return "VENDEUR_VENDEUR";

        boolean vendeurLivreur = (roleMoi.equals("VENDEUR") && roleAutre.equals("LIVREUR"))
                || (roleMoi.equals("LIVREUR") && roleAutre.equals("VENDEUR"));
        if (vendeurLivreur) {
            Utilisateur vendeur = roleMoi.equals("VENDEUR") ? moi : autre;
            Utilisateur livreur = roleMoi.equals("VENDEUR") ? autre : moi;
            if (!livreurAccessible(livreur, vendeur.getId())) {
                throw new AccessDeniedException("Ce livreur ne fait pas partie de votre équipe ni d'un groupe que vous avez rejoint");
            }
            return "VENDEUR_LIVREUR";
        }

        throw new AccessDeniedException("Cette combinaison de comptes ne peut pas échanger de messages");
    }

    /** Même règle d'accès que pour l'assignation d'une commande à un livreur — voir CommandeService. */
    private boolean livreurAccessible(Utilisateur livreur, Long vendeurId) {
        if (livreur.getVendeurProprietaire() == null) return false;
        if (livreur.getVendeurProprietaire().getId().equals(vendeurId)) return true;
        return groupeLivraisonRepository.findByCreateur_Id(livreur.getVendeurProprietaire().getId()).stream()
                .anyMatch(g -> g.getMembres().stream().anyMatch(m -> m.getId().equals(vendeurId)));
    }

    public List<ConversationDto> mesConversations() {
        Long moiId = SecurityUtils.getCurrentUserId();
        return conversationRepository.findByParticipant(moiId).stream()
                .map(c -> toDto(c, moiId))
                .sorted(Comparator.comparing(ConversationDto::getDernierMessageDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MessageDto> messagesDe(Long conversationId) {
        Long moiId = SecurityUtils.getCurrentUserId();
        Conversation conv = getConversationSiParticipant(conversationId, moiId);
        List<Message> messages = messageRepository.findByConversation_IdOrderByDateEnvoiAsc(conversationId);
        // Marque comme lus tous les messages de l'autre personne
        messages.stream()
                .filter(m -> !m.getExpediteur().getId().equals(moiId) && !m.getLu())
                .forEach(m -> m.setLu(true));
        messageRepository.saveAll(messages);
        return messages.stream().map(m -> toMessageDto(m, moiId)).collect(Collectors.toList());
    }

    @Transactional
    public MessageDto envoyer(Long conversationId, String contenu) {
        if (contenu == null || contenu.isBlank()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide");
        }
        Long moiId = SecurityUtils.getCurrentUserId();
        Conversation conv = getConversationSiParticipant(conversationId, moiId);
        Utilisateur moi = utilisateurRepository.findById(moiId).orElseThrow();
        Message m = new Message();
        m.setConversation(conv);
        m.setExpediteur(moi);
        m.setContenu(contenu.trim());
        return toMessageDto(messageRepository.save(m), moiId);
    }

    private Conversation getConversationSiParticipant(Long conversationId, Long moiId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));
        boolean participant = conv.getParticipant1().getId().equals(moiId) || conv.getParticipant2().getId().equals(moiId);
        if (!participant) {
            throw new AccessDeniedException("Vous ne faites pas partie de cette conversation");
        }
        return conv;
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private ConversationDto toDto(Conversation c, Long moiId) {
        ConversationDto dto = new ConversationDto();
        dto.setId(c.getId());
        dto.setType(c.getType());
        Utilisateur autre = c.getParticipant1().getId().equals(moiId) ? c.getParticipant2() : c.getParticipant1();
        dto.setAutreParticipantId(autre.getId());
        dto.setAutreParticipantNom(autre.getPrenom() + " " + autre.getNom());
        dto.setAutreParticipantRole(autre.getTypeUtilisateur().getNom_utilisateur().toUpperCase());
        Produit p = c.getProduitContexte();
        if (p != null) dto.setProduitContexteNom(p.getNom_produit());

        List<Message> messages = messageRepository.findByConversation_IdOrderByDateEnvoiAsc(c.getId());
        if (!messages.isEmpty()) {
            Message dernier = messages.get(messages.size() - 1);
            dto.setDernierMessage(dernier.getContenu());
            dto.setDernierMessageDate(dernier.getDateEnvoi().format(FMT));
        }
        long nonLus = messages.stream().filter(m -> !m.getExpediteur().getId().equals(moiId) && !m.getLu()).count();
        dto.setNbNonLus(nonLus);
        return dto;
    }

    private MessageDto toMessageDto(Message m, Long moiId) {
        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setExpediteurId(m.getExpediteur().getId());
        dto.setExpediteurNom(m.getExpediteur().getPrenom() + " " + m.getExpediteur().getNom());
        dto.setContenu(m.getContenu());
        dto.setDateEnvoi(m.getDateEnvoi().format(FMT));
        dto.setDeMoi(m.getExpediteur().getId().equals(moiId));
        return dto;
    }
}
