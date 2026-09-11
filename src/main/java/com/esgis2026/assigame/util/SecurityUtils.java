package com.esgis2026.assigame.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Retourne l'id de l'utilisateur connecté, ou null si personne n'est
     * connecté (visiteur). Important : quand aucun JWT n'est envoyé, Spring
     * Security remplit quand même le contexte avec une authentification
     * "anonyme" dont le principal est la chaîne "anonymousUser" (pas null,
     * pas un Long) — un cast direct en Long plantait alors avec un
     * ClassCastException dès qu'un visiteur non connecté passait une
     * commande "invité". On vérifie donc le type avant de caster.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return null;
        }
        return (Long) auth.getPrincipal();
    }

    /** Retourne le rôle courant sans le préfixe "ROLE_" (ex: "VENDEUR", "ADMIN", "CLIENT"). */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return null;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            String authority = a.getAuthority();
            if (authority != null && authority.startsWith("ROLE_")) {
                return authority.substring(5);
            }
        }
        return null;
    }

    public static boolean isAdmin()   { return "ADMIN".equalsIgnoreCase(getCurrentUserRole()); }
    public static boolean isVendeur() { return "VENDEUR".equalsIgnoreCase(getCurrentUserRole()); }
    public static boolean isClient()  { return "CLIENT".equalsIgnoreCase(getCurrentUserRole()); }
    public static boolean isLivreur() { return "LIVREUR".equalsIgnoreCase(getCurrentUserRole()); }
}
