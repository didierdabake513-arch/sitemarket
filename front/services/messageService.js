/* =============================================================
   services/messageService.js
   Messagerie privée — acheteur↔vendeur, vendeur↔vendeur, vendeur↔livreur.
   Confidentielle (voir MessageService côté back), pas de chiffrement de
   bout en bout dans cette version.
   ============================================================= */

async function demarrerConversation({ autreUtilisateurId, autreEmail, produitId } = {}) {
  return await POST("/messages/conversations", { autreUtilisateurId, autreEmail, produitId });
}
/* Contacter l'administrateur — utilisable par n'importe quel compte connecté
   (client, vendeur, livreur). Ouvre la conversation existante s'il y en a déjà une. */
async function contacterAdmin() {
  return await POST("/messages/conversations/admin", {});
}
async function getMesConversations() {
  const { data, error } = await GET("/messages/conversations");
  if (error) return { data: [], error };
  return { data, error: null };
}
async function getMessagesDe(conversationId) {
  const { data, error } = await GET(`/messages/conversations/${conversationId}`);
  if (error) return { data: [], error };
  return { data, error: null };
}
async function envoyerMessage(conversationId, contenu) {
  return await POST(`/messages/conversations/${conversationId}`, { contenu });
}
