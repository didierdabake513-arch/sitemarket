/* =============================================================
   utils/icons.js
   Icônes système (Font Awesome) — support image uploadée + emoji fallback.
   ============================================================= */

function faIcon(className, extraClass = "") {
  return `<i class="fa-solid ${className}${extraClass ? " " + extraClass : ""}" aria-hidden="true"></i>`;
}

/**
 * Retourne le HTML de l'icône du produit :
 *   • <img> si le produit a une imageBase64 ou imageUrl — remplit tout
 *     son conteneur par défaut (les pages définissent la taille du
 *     conteneur via CSS : .product-img, .detail-img-box, .cart-emoji…)
 *   • emoji texte sinon (rétro-compatible)
 * @param {object} product - objet produit { imageBase64?, imageUrl?, emoji? }
 * @param {string} size    - taille CSS explicite (ex. "22px") ; par défaut
 *                            "100%" pour remplir le conteneur parent.
 */
function productIcon(product, size = "100%") {
  if (!product) return "📦";
  const src = product.imageBase64 || product.imageUrl || null;
  if (src) {
    return `<img src="${src}" alt="${product.nom || 'produit'}"
                 style="width:${size};height:${size};object-fit:cover;border-radius:inherit;display:block;"
                 loading="lazy">`;
  }
  return (product.emoji) ? product.emoji : "📦";
}

/** Alias rétro-compatible utilisé dans Commande.html */
function productEmoji(product) {
  return productIcon(product);
}

/**
 * Charge la liste des images présentes dans un dossier du serveur
 * (voir ImagesController côté back : GET /api/images/{dossier}) et fait
 * défiler un jeu de <div class="…-bg"> en fondu. Pour ajouter une image
 * au carrousel, il suffit de déposer un fichier dans le dossier
 * correspondant côté back — rien à changer ici.
 *
 * @param {string} dossier    - nom du dossier côté back (ex. "commerce", "regions")
 * @param {string} hostId     - id de l'élément conteneur où injecter les slides
 * @param {string} slideClass - classe CSS à donner à chaque slide (avec .active en cycle)
 * @param {number} intervalMs - délai entre deux images
 * @returns {Promise<string[]>} la liste des URLs chargées (peut être vide)
 */
async function loadImageFolderCarousel(dossier, hostId, slideClass, intervalMs = 4500) {
  const host = document.getElementById(hostId);
  if (!host) return [];
  try {
    const res = await fetch(`/api/images/${dossier}`);
    const urls = res.ok ? await res.json() : [];
    if (!urls.length) return [];
    host.innerHTML = urls.map((u, i) => `<div class="${slideClass} ${i===0?'active':''}" style="background-image:url('${u}')"></div>`).join("");
    if (urls.length > 1) {
      const slides = host.querySelectorAll(`.${slideClass}`);
      let current = 0;
      setInterval(() => {
        slides[current].classList.remove("active");
        current = (current + 1) % slides.length;
        slides[current].classList.add("active");
      }, intervalMs);
    }
    return urls;
  } catch (e) {
    return [];
  }
}
