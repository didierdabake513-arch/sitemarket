package com.esgis2026.assigame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationDto {
    private Long id;
    private String type;
    private Long autreParticipantId;
    private String autreParticipantNom;
    private String autreParticipantRole;
    private String produitContexteNom;
    private String dernierMessage;
    private String dernierMessageDate;
    private Long nbNonLus;
}
