/* =============================================================
   services/authService.js
   Authentification — Login · Register · Logout · Devenir vendeur
   ============================================================= */

async function login(email, password) {
  const { data, error } = await POST_PUBLIC("/auth/login", { email, password });
  if (error) return { user: null, error };
  setToken(data.token);
  setCurrentUser(data.user);
  return { user: data.user, error: null };
}

async function register(nom, prenom, email, telephone, password, region) {
  const { data, error } = await POST_PUBLIC("/auth/register", { nom, prenom, email, telephone, password, region });
  if (error) return { user: null, error };
  setToken(data.token);
  setCurrentUser(data.user);
  return { user: data.user, error: null };
}

async function logout() {
  try { await POST("/auth/logout"); } catch (e) { /* session locale nettoyée quand même */ }
  clearSession();
}

async function becomeVendeur() {
  let { data, error } = await PUT("/utilisateurs/me/devenir-vendeur");
  if (error && (String(error).includes("404") || error === "Not Found")) {
    ({ data, error } = await POST("/utilisateurs/me/devenir-vendeur"));
  }
  if (error) return { user: null, error };
  /* Nouveau JWT avec rôle VENDEUR — obligatoire sinon les APIs vendeur restent en 403. */
  if (data.token) setToken(data.token);
  setCurrentUser(data.user || data);
  return { user: getCurrentUser(), error: null };
}
