package com.esgis2026.assigame.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.ConversationDto;
import com.esgis2026.assigame.dto.MessageDto;
import com.esgis2026.assigame.service.MessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** Démarre une conversation avec un autre utilisateur, ou récupère celle qui existe déjà. */
    @PostMapping("/conversations")
    public ConversationDto demarrerOuRecuperer(@RequestBody Map<String, Object> body) {
        Long autreId = body.get("autreUtilisateurId") != null ? Long.valueOf(String.valueOf(body.get("autreUtilisateurId"))) : null;
        String autreEmail = body.get("autreEmail") != null ? String.valueOf(body.get("autreEmail")) : null;
        Long produitId = body.get("produitId") != null ? Long.valueOf(String.valueOf(body.get("produitId"))) : null;
        return messageService.demarrerOuRecuperer(autreId, autreEmail, produitId);
    }

    @GetMapping("/conversations")
    public List<ConversationDto> mesConversations() {
        return messageService.mesConversations();
    }

    /** Contacter l'administrateur — ouvre ou récupère la conversation existante. */
    @PostMapping("/conversations/admin")
    public ConversationDto contacterAdmin() {
        return messageService.contacterAdmin();
    }

    @GetMapping("/conversations/{id}")
    public List<MessageDto> messagesDe(@PathVariable Long id) {
        return messageService.messagesDe(id);
    }

    @PostMapping("/conversations/{id}")
    public MessageDto envoyer(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return messageService.envoyer(id, body.get("contenu"));
    }
}
