package com.esgis2026.assigame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
    private Long id;
    private Long expediteurId;
    private String expediteurNom;
    private String contenu;
    private String dateEnvoi;
    private Boolean deMoi;
}
