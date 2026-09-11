/* =============================================================
   services/orderService.js
   Commandes — création, historique, changement de statut

   ⚙️ ENDPOINTS SPRING BOOT ATTENDUS :
     POST /api/commandes                   { adresseLivraison, telephone,
                                              modePaiement, items[], montantTotal }
                                              → { reference }
                                              (le backend recalcule le total
                                              côté serveur, ne fait jamais confiance
                                              au montantTotal envoyé par le front)
     GET  /api/commandes/me                → commandes du CLIENT connecté
     GET  /api/commandes                   → toutes les commandes (ADMIN)
     GET  /api/commandes/vendeur/me        → commandes contenant au moins un produit
                                              du VENDEUR connecté (filtré côté back)
     PUT  /api/commandes/{id}/statut        { statut }  → ADMIN et VENDEUR (sur ses
                                              propres commandes uniquement)

   Statuts : EN_ATTENTE · EXPEDIEE · LIVREE · ANNULEE
   Paiement : MOBILE_MONEY · CARTE · LIVRAISON
   ============================================================= */

async function createOrder(payload) {
  /* Invité : sans token. Connecté : avec token pour lier la commande au compte.
     Ne jamais forcer POST seul : un token absent/invalide casserait le checkout visiteur. */
  if (typeof getToken === "function" && getToken()) {
    return await POST("/commandes", payload);
  }
  return await POST_PUBLIC("/commandes", payload);
}

async function getMyOrders() {
  const { data, error } = await GET("/commandes/me");
  if (error) return { data: [], error };
  return { data, error: null };
}

async function getAllOrders() {
  const { data, error } = await GET("/commandes");
  if (error) return { data: [], error };
  return { data, error: null };
}

/* Commandes contenant des produits du vendeur connecté */
async function getVendeurOrders() {
  const { data, error } = await GET("/commandes/vendeur/me");
  if (error) return { data: [], error };
  return { data, error: null };
}

async function updateOrderStatus(id, statut) {
  return await PUT(`/commandes/${id}/statut`, { statut });
}

/* ── Livreurs — créés par le vendeur, assignés aux commandes, QR de livraison ── */
async function assignerLivreur(commandeId, livreurId) {
  return await PUT(`/commandes/${commandeId}/assigner-livreur?livreurId=${livreurId}`, {});
}
async function getMesLivraisons() {
  const { data, error } = await GET("/commandes/mes-livraisons");
  if (error) return { data: [], error };
  return { data, error: null };
}
async function scannerLivraison(codeLivraison) {
  return await POST("/commandes/scanner-livraison", { codeLivraison });
}
async function creerLivreur(payload) {
  return await POST("/livreurs", payload);
}
async function getMesLivreurs() {
  const { data, error } = await GET("/livreurs/mes-livreurs");
  if (error) return { data: [], error };
  return { data, error: null };
}
async function toggleLivreurBlocage(id, bloque) {
  return await PUT(`/livreurs/${id}/bloquer?bloque=${bloque}`, {});
}
async function getLivreursDisponibles() {
  const { data, error } = await GET("/livreurs/disponibles");
  if (error) return { data: [], error };
  return { data, error: null };
}
async function fairePartirTournee(livreurId) {
  return await PUT(`/commandes/livreur/${livreurId}/faire-partir`, {});
}

/* ── Groupes de livraison entre vendeurs (libre, sans validation admin) ── */
async function creerGroupeLivraison(nom) {
  return await POST("/groupes-livraison", { nom });
}
async function getTousLesGroupes() {
  const { data, error } = await GET("/groupes-livraison");
  if (error) return { data: [], error };
  return { data, error: null };
}
async function getMesGroupes() {
  const { data, error } = await GET("/groupes-livraison/mes-groupes");
  if (error) return { data: [], error };
  return { data, error: null };
}
async function rejoindreGroupe(id) {
  return await POST(`/groupes-livraison/${id}/rejoindre`, {});
}
async function quitterGroupe(id) {
  return await DELETE(`/groupes-livraison/${id}/quitter`);
}

/* ── Envoi email de confirmation commande ──
   ⚙️ ENDPOINT SPRING BOOT : POST /api/commandes/{reference}/confirmation-email
   Le backend génère l'email HTML et l'envoie via JavaMailSender.
   Payload optionnel : { emailHtml } pour un template personnalisé.          */
async function sendOrderConfirmationEmail(reference, emailPayload = {}) {
  const { data, error } = await POST(`/commandes/${reference}/confirmation-email`, emailPayload);
  if (error === "NETWORK_ERROR") return { sent: false, offline: true };
  if (error) return { sent: false, error };
  return { sent: true, data };
}

/* ── Envoi email d'invitation admin ──
   ⚙️ ENDPOINT SPRING BOOT : POST /api/admin/invitations
   { email, nom, prenom, role } — le backend envoie le lien d'activation.   */
async function sendAdminInvitation(email, nom, prenom, role = "ADMIN") {
  const { data, error } = await POST("/admin/invitations", { email, nom, prenom, role });
  if (error === "NETWORK_ERROR") return { sent: false, offline: true };
  if (error) return { sent: false, error };
  return { sent: true, data };
}
