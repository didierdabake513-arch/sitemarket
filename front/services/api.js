/* =============================================================
   services/api.js
   Client HTTP central — toutes les requêtes passent par ici.
   Doit être chargé EN PREMIER dans chaque page (avant les autres
   services qui utilisent GET/POST/PUT/DELETE).
   ============================================================= */

const API_BASE_URL = "http://localhost:8090/api";

function getToken()      { return localStorage.getItem("assigame_token"); }
function setToken(token)  { localStorage.setItem("assigame_token", token); }
function removeToken()   { localStorage.removeItem("assigame_token"); }

/* Identifiant utilisateur lu directement dans le token JWT (localStorage,
   toujours disponible), plutôt que dans assigame_user (sessionStorage,
   propre à chaque onglet et pas toujours encore rempli à l'ouverture
   d'une page). Sert de clé stable pour le panier — voir cartStorageKey()
   dans utils/toast.js : sans ça, un panier pouvait être écrit sous une
   clé et relu sous une autre, rendant la suppression d'article muette. */
function getUserIdFromToken() {
  const token = getToken();
  if (!token) return null;
  try {
    const payload = token.split(".")[1];
    const json = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    return json.sub || null;
  } catch (_) { return null; }
}

function getCurrentUser() {
  try { return JSON.parse(sessionStorage.getItem("assigame_user")); }
  catch (_) { return null; }
}
function setCurrentUser(user) { sessionStorage.setItem("assigame_user", JSON.stringify(user)); }
function clearSession() {
  localStorage.removeItem("assigame_token");
  sessionStorage.removeItem("assigame_user");
}

function isLoggedIn() { return !!getToken(); }
function isAdmin()    { const u = getCurrentUser(); return u && u.role === "ADMIN"; }
function isVendeur()  { const u = getCurrentUser(); return u && u.role === "VENDEUR"; }
function isClient()   { const u = getCurrentUser(); return u && u.role === "CLIENT"; }

function buildHeaders(withAuth = true) {
  const headers = { "Content-Type": "application/json" };
  if (withAuth) {
    const token = getToken();
    if (token) headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
}

async function apiCall(method, endpoint, body = null, withAuth = true) {
  const options = { method, headers: buildHeaders(withAuth) };
  if (body) options.body = JSON.stringify(body);

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

    if (response.status === 401) {
      clearSession();
      return { data: null, error: "UNAUTHORIZED" };
    }

    if (!response.ok) {
      let errorMsg = `Erreur ${response.status}`;
      try { const errBody = await response.json(); errorMsg = errBody.message || errBody.error || errorMsg; }
      catch (_) { /* pas de corps JSON */ }
      return { data: null, error: errorMsg };
    }

    if (response.status === 204) return { data: null, error: null };

    const data = await response.json();
    return { data, error: null };

  } catch (err) {
    console.warn("[API] Backend inaccessible :", err.message);
    return { data: null, error: "NETWORK_ERROR" };
  }
}

const GET           = (url)       => apiCall("GET",    url);
const POST          = (url, body) => apiCall("POST",   url, body);
const PUT            = (url, body) => apiCall("PUT",    url, body);
const DELETE         = (url)       => apiCall("DELETE", url);
const POST_PUBLIC    = (url, body) => apiCall("POST", url, body, false);
const GET_PUBLIC     = (url)       => apiCall("GET",  url, null, false);
