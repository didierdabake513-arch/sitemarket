/* =============================================================
   services/productService.js
   Produits — GET / POST / PUT / DELETE

   ⚙️ ENDPOINTS SPRING BOOT ATTENDUS :
     GET    /api/produits                  → tous les produits (vue publique)
     GET    /api/produits/{id}             → un produit
     GET    /api/produits/me               → produits du VENDEUR connecté uniquement
                                              (le backend doit filtrer par vendeurId
                                              à partir du token JWT, jamais confier ce
                                              filtre au front)
     POST   /api/produits                  → créer (CLIENT interdit, VENDEUR/ADMIN ok ;
                                              le backend doit associer vendeurId = user
                                              courant automatiquement si rôle VENDEUR)
     PUT    /api/produits/{id}             → modifier (vérifier côté back que le
                                              vendeur ne modifie QUE ses propres produits)
     DELETE /api/produits/{id}             → supprimer (même vérification)
   ============================================================= */

async function getAllProducts() {
  const { data, error } = await GET_PUBLIC("/produits");
  if (error) return { data: [], error };
  return { data, error: null };
}

async function getProductById(id) {
  const { data, error } = await GET_PUBLIC(`/produits/${id}`);
  if (error) return { data: null, error };
  return { data, error: null };
}

/* Produits du vendeur connecté uniquement */
async function getMyProducts() {
  const { data, error } = await GET("/produits/me");
  if (error) return { data: [], error };
  return { data, error: null };
}

async function createProduct(produit) {
  /* produit : { nom, prix, categorie, description, stock, emoji, prixBarre, enPromotion } */
  return await POST("/produits", produit);
}

async function updateProduct(id, produit) {
  return await PUT(`/produits/${id}`, produit);
}

async function deleteProduct(id) {
  return await DELETE(`/produits/${id}`);
}

/* Dashboard Admin uniquement : absolument tous les produits, y compris
   ceux supprimés (affichés grisés avec un bouton Restaurer). */
async function getAllProductsForAdmin() {
  const { data, error } = await GET("/produits/admin/all");
  if (error) return { data: [], error };
  return { data, error: null };
}

/* Annule une suppression — réservé à l'admin. */
async function restoreProduct(id) {
  return await PUT(`/produits/${id}/restaurer`, {});
}

/* Met un produit en avant sur l'accueil (ou retire cette mise en avant) —
   réservé à l'admin. */
async function setProductFeatured(id, value) {
  return await PUT(`/produits/${id}/mettre-en-avant?value=${value}`, {});
}
