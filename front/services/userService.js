/* =============================================================
   services/userService.js
   Utilisateurs — profil personnel + gestion admin

   ⚙️ ENDPOINTS SPRING BOOT ATTENDUS :
     GET    /api/utilisateurs/me                  → profil connecté
     PUT    /api/utilisateurs/me                   { nom, prenom, email,
                                                       telephone, dateNaissance, adresse }
     PUT    /api/utilisateurs/me/password           { oldPassword, newPassword }
     PUT    /api/utilisateurs/me/notifications       { preferences[] }
     DELETE /api/utilisateurs/me                    → suppression du compte

     GET    /api/utilisateurs                       → tous les utilisateurs (ADMIN)
     PUT    /api/utilisateurs/{id}/bloquer           { bloquer: bool }       (ADMIN)
     DELETE /api/utilisateurs/{id}                   → suppression (ADMIN)

     -- Invitation admin (jamais d'auto-inscription admin) --
     POST   /api/admin/invitations                   { email }  (ADMIN uniquement)
                                                       → envoie un email avec un lien
                                                       à usage unique qui élève le
                                                       compte correspondant en ADMIN
                                                       lors du clic (logique 100% back).
   ============================================================= */

async function getMyProfile() {
  const { data, error } = await GET("/utilisateurs/me");
  if (error) return { data: null, error };
  return { data, error: null };
}

async function updateMyProfile(profile) {
  return await PUT("/utilisateurs/me", profile);
}

async function changeMyPassword(oldPassword, newPassword) {
  return await PUT("/utilisateurs/me/password", { oldPassword, newPassword });
}

async function updateMyNotifications(preferences) {
  return await PUT("/utilisateurs/me/notifications", { preferences });
}

async function deleteMyAccount() {
  return await DELETE("/utilisateurs/me");
}

/* ── Admin uniquement ── */
async function getAllUsers() {
  const { data, error } = await GET("/utilisateurs");
  if (error) return { data: [], error };
  return { data, error: null };
}

async function toggleBlockUser(id, bloquer) {
  return await PUT(`/utilisateurs/${id}/bloquer`, { bloquer });
}

async function deleteUser(id) {
  return await DELETE(`/utilisateurs/${id}`);
}
