/* =============================================================
   utils/featureGate.js
   Bloque les fonctionnalités nécessitant une API externe.
   Affiche une fenêtre modale élégante "Prochaine version".
   ============================================================= */

/* ── Injecte les styles une seule fois ── */
(function _injectStyles() {
  if (document.getElementById("fg-modal-styles")) return;
  const s = document.createElement("style");
  s.id = "fg-modal-styles";
  s.textContent = `
    /* ═══════════ Overlay "Bientôt disponible" ═══════════ */
    #fg-overlay {
      position: fixed; inset: 0; z-index: 9999;
      background: rgba(20, 10, 60, 0.55);
      backdrop-filter: blur(8px);
      display: flex; align-items: center; justify-content: center;
      opacity: 0; pointer-events: none;
      transition: opacity 0.25s ease;
    }
    #fg-overlay.fg-visible {
      opacity: 1; pointer-events: all;
    }
    #fg-card {
      background: #fff;
      border-radius: 22px;
      padding: 2.5rem 2rem 2rem;
      max-width: 400px; width: 90%;
      box-shadow: 0 24px 64px rgba(45,27,105,0.28);
      text-align: center;
      transform: translateY(30px) scale(0.95);
      transition: transform 0.35s cubic-bezier(0.34,1.56,0.64,1);
    }
    #fg-overlay.fg-visible #fg-card {
      transform: translateY(0) scale(1);
    }
    #fg-icon-wrap {
      width: 76px; height: 76px; border-radius: 50%;
      background: linear-gradient(135deg, #2D1B69 0%, #7C5CBF 100%);
      display: flex; align-items: center; justify-content: center;
      margin: 0 auto 1.25rem;
      box-shadow: 0 8px 24px rgba(45,27,105,0.3);
      animation: fg-float 3s ease-in-out infinite;
    }
    @keyframes fg-float {
      0%,100% { transform: translateY(0); }
      50%      { transform: translateY(-6px); }
    }
    #fg-icon-wrap svg { width: 36px; height: 36px; }
    #fg-title {
      font-family: 'Sora', 'Inter', sans-serif;
      font-size: 1.35rem; font-weight: 800;
      color: #2D1B69; margin-bottom: 0.4rem;
    }
    #fg-feature-name {
      display: inline-block;
      background: linear-gradient(135deg, #F0EEFB, #E3DEFA);
      color: #5540A8; font-size: 0.8rem; font-weight: 700;
      border-radius: 20px; padding: 3px 12px; margin-bottom: 0.9rem;
      letter-spacing: 0.03em; text-transform: uppercase;
    }
    #fg-feature-name:empty { display: none; }
    #fg-msg {
      font-family: 'Inter', sans-serif;
      font-size: 0.9rem; color: #6060A0; line-height: 1.6;
      margin-bottom: 1.6rem;
    }
    #fg-dots {
      display: flex; justify-content: center; gap: 6px; margin-bottom: 1.6rem;
    }
    #fg-dots span {
      width: 8px; height: 8px; border-radius: 50%;
      background: #DDD9F5;
      animation: fg-dot-blink 1.4s infinite ease-in-out;
    }
    #fg-dots span:nth-child(2) { animation-delay: 0.2s; }
    #fg-dots span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes fg-dot-blink {
      0%,80%,100% { background: #DDD9F5; transform: scale(1); }
      40%          { background: #7C5CBF; transform: scale(1.3); }
    }
    #fg-close-btn {
      display: inline-flex; align-items: center; gap: 6px;
      background: linear-gradient(135deg, #2D1B69, #5540A8);
      color: #fff; border: none; border-radius: 10px;
      padding: 0.65rem 1.6rem; font-size: 0.9rem; font-weight: 700;
      cursor: pointer; transition: opacity 0.2s, transform 0.15s;
      font-family: 'Inter', sans-serif;
    }
    #fg-close-btn:hover { opacity: 0.88; transform: translateY(-1px); }
  `;
  document.head.appendChild(s);
})();

/* ── Crée le DOM de la modal une seule fois ── */
function _ensureFgModal() {
  if (document.getElementById("fg-overlay")) return;
  const el = document.createElement("div");
  el.id = "fg-overlay";
  el.setAttribute("role", "dialog");
  el.setAttribute("aria-modal", "true");
  el.innerHTML = `
    <div id="fg-card">
      <div id="fg-icon-wrap">
        <!-- Rocket SVG -->
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2C12 2 7 6.5 7 13C7 16.5 9 19 12 20C15 19 17 16.5 17 13C17 6.5 12 2 12 2Z"
                fill="white" opacity="0.9"/>
          <circle cx="12" cy="13" r="2" fill="#7C5CBF"/>
          <path d="M9 20L7 23M15 20L17 23" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M8 15C6 15 4 14 4 12L7 13" fill="white" opacity="0.6"/>
          <path d="M16 15C18 15 20 14 20 12L17 13" fill="white" opacity="0.6"/>
        </svg>
      </div>
      <div id="fg-title">Bientôt disponible ✨</div>
      <div id="fg-feature-name"></div>
      <div id="fg-msg">
        Cette fonctionnalité arrive dans la <strong>prochaine version</strong> d'Assigamé.<br>
        Revenez bientôt !
      </div>
      <div id="fg-dots">
        <span></span><span></span><span></span>
      </div>
      <button id="fg-close-btn" onclick="closeFgModal()">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M5 12H19" stroke="white" stroke-width="2.5" stroke-linecap="round"/></svg>
        Compris !
      </button>
    </div>
  `;
  /* Clic sur l'overlay ferme aussi la modal */
  el.addEventListener("click", (e) => { if (e.target === el) closeFgModal(); });
  document.body.appendChild(el);
}

/** Ouvre la modal "Bientôt disponible" avec un nom de fonctionnalité optionnel */
function showComingSoonModal(featureName = "") {
  _ensureFgModal();
  const nameEl = document.getElementById("fg-feature-name");
  if (nameEl) nameEl.textContent = featureName || "";
  const overlay = document.getElementById("fg-overlay");
  overlay.classList.add("fg-visible");
  /* Piège le focus sur le bouton Fermer */
  setTimeout(() => document.getElementById("fg-close-btn")?.focus(), 100);
}

/** Ferme la modal */
function closeFgModal() {
  const overlay = document.getElementById("fg-overlay");
  if (overlay) overlay.classList.remove("fg-visible");
}

/** Rétro-compatible : conserve la signature d'origine (event, featureName) */
function showComingSoon(event, featureName = "") {
  if (event) { event.preventDefault(); event.stopPropagation(); }
  showComingSoonModal(featureName);
  return false;
}

/** Modes de paiement nécessitant une passerelle externe */
const GATED_PAYMENTS = new Set(["MOBILE_MONEY", "CARTE"]);
function isGatedPayment(mode) { return GATED_PAYMENTS.has(mode); }

/** Préférences notifications nécessitant email / SMS / push réel */
const GATED_NOTIF_IDS = new Set(["n1", "n2", "n3", "n4", "n5", "n6", "n7"]);
function isGatedNotification(notifId) { return GATED_NOTIF_IDS.has(notifId); }
