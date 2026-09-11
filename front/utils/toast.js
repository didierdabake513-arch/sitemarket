/* =============================================================
   utils/toast.js
   Composant Toast — notification non bloquante
   ============================================================= */

let _toastTimer = null;

function showToast(msg, type = "", duration = 3000) {
  let el = document.getElementById("app-toast");
  if (!el) {
    el = document.createElement("div");
    el.id = "app-toast";
    el.className = "toast";
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.className = `toast ${type} show`;
  clearTimeout(_toastTimer);
  _toastTimer = setTimeout(() => { el.className = "toast"; }, duration);
}


/* =============================================================
   utils/validator.js
   Validation des formulaires + force du mot de passe
   ============================================================= */

function isValidEmail(email) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim()); }
function isValidPhone(phone) { return phone.trim().replace(/\s/g, "").length >= 8; }

function showFieldError(errorId, message) {
  const el = document.getElementById(errorId);
  if (!el) return;
  el.textContent = message;
  el.classList.add("show");
}
function clearFieldError(errorId) {
  const el = document.getElementById(errorId);
  if (el) el.classList.remove("show");
}

function checkPasswordStrength(pwd) {
  let score = 0;
  if (pwd.length >= 8)          score++;
  if (/[A-Z]/.test(pwd))        score++;
  if (/[0-9]/.test(pwd))        score++;
  if (/[^A-Za-z0-9]/.test(pwd)) score++;
  const levels = [
    { label: "Très faible", color: "#E74C3C", pct: "25%" },
    { label: "Faible",      color: "#E67E22", pct: "50%" },
    { label: "Moyen",       color: "#F5A623", pct: "75%" },
    { label: "Fort",        color: "#27AE60", pct: "100%" },
  ];
  return { score, ...levels[Math.max(0, score - 1)] };
}

function updateStrengthBar(barId, labelId, pwd) {
  const bar = document.getElementById(barId), label = document.getElementById(labelId);
  if (!bar || !label) return;
  if (!pwd) { bar.style.width = "0"; label.textContent = ""; return; }
  const result = checkPasswordStrength(pwd);
  bar.style.width = result.pct; bar.style.background = result.color; label.textContent = result.label;
}


/* =============================================================
   utils/cart.js
   Panier — stocké côté client en localStorage, PAR COMPTE : chaque
   utilisateur (et les invités non connectés) a sa propre clé, pour que
   changer de compte sur le même navigateur ne montre jamais le panier
   de quelqu'un d'autre.
   Format : [{ id: <produitId>, quantite: <n> }, ...]

   ⚙️ NOTE BACKEND : au moment de POST /api/commandes, Spring Boot
   doit revalider le stock et recalculer chaque ligne de prix
   depuis la base — ne jamais faire confiance aux prix envoyés
   par le front (un utilisateur pourrait modifier le JS local).
   ============================================================= */

function cartStorageKey() {
  const uid = getUserIdFromToken();
  return uid ? `assigame_cart_${uid}` : "assigame_cart_guest";
}

function getCart() {
  /* Le stockage renvoie parfois une valeur qui n'est PAS un tableau
     (donnée corrompue ou d'un ancien format) — JSON.parse réussit alors
     sans lever d'erreur, et `.reduce`/`.map` plantaient plus loin en
     silence, bloquant net toute la page (ex. Catalogue.html restait
     coincé sur "Chargement des produits…"). On se protège en vérifiant
     explicitement que c'est bien un tableau, sinon on repart de zéro. */
  try {
    const parsed = JSON.parse(localStorage.getItem(cartStorageKey()));
    return Array.isArray(parsed) ? parsed : [];
  } catch (_) { return []; }
}
function setCart(cart) {
  localStorage.setItem(cartStorageKey(), JSON.stringify(cart));
  updateCartBadge();
}
function clearCart() { setCart([]); }

function addToCart(productId, quantite = 1) {
  const cart = getCart();
  const existing = cart.find(i => Number(i.id) === Number(productId));
  if (existing) existing.quantite += quantite;
  else cart.push({ id: Number(productId), quantite });
  setCart(cart);
}
function removeFromCart(productId) {
  setCart(getCart().filter(i => i.id !== productId));
}
function updateCartQty(productId, quantite) {
  const cart = getCart();
  const item = cart.find(i => i.id === productId);
  if (item) item.quantite = Math.max(1, quantite);
  setCart(cart);
}

/* Met à jour le badge "cart-count" si présent sur la page */
function updateCartBadge() {
  const el = document.getElementById("cart-count");
  if (!el) return;
  try { el.textContent = getCart().reduce((s, i) => s + (i.quantite||0), 0); }
  catch (e) { console.warn("[updateCartBadge] échec, badge remis à 0 :", e); el.textContent = 0; }
}


/* =============================================================
   Suivi des commandes "invité" (sans compte).
   Un visiteur qui commande sans se connecter n'a aucun moyen de
   retrouver sa commande une fois reparti (pas de compte, pas
   d'historique côté serveur pour lui). On garde donc une trace
   légère côté navigateur : référence + code de livraison (QR) +
   date. Affichée en bas de la page Panier.
   Ce mécanisme fonctionne uniquement sur le même navigateur/appareil
   que celui utilisé pour commander.
   ============================================================= */
const GUEST_ORDERS_KEY = "assigame_guest_orders";

function getGuestOrders() {
  try {
    const parsed = JSON.parse(localStorage.getItem(GUEST_ORDERS_KEY));
    return Array.isArray(parsed) ? parsed : [];
  } catch (_) { return []; }
}

function saveGuestOrder({ reference, codeLivraison, total, date }) {
  if (!reference) return;
  const orders = getGuestOrders();
  if (orders.some(o => o.reference === reference)) return;
  orders.unshift({ reference, codeLivraison, total, date: date || new Date().toISOString() });
  /* on garde une liste raisonnable, pas un historique infini */
  localStorage.setItem(GUEST_ORDERS_KEY, JSON.stringify(orders.slice(0, 20)));
}


function togglePwd(inputId, btn) {
  const inp = document.getElementById(inputId);
  if (!inp || !btn) return;
  const show = inp.type === "password";
  inp.type = show ? "text" : "password";
  btn.innerHTML = show
    ? '<i class="fa-solid fa-eye-slash"></i>'
    : '<i class="fa-solid fa-eye"></i>';
}
