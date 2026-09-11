/* =============================================================
   services/statsService.js
   Statistiques — Dashboard Admin ET Dashboard Vendeur

   ⚙️ ENDPOINTS SPRING BOOT ATTENDUS (idéal — calcul fait côté back) :
     GET /api/stats/summary                  → { totalProducts, totalOrders,
                                                   totalUsers, totalRevenue }   (ADMIN)
     GET /api/stats/revenue-monthly?year=2025 → [{ mois, montant }]            (ADMIN)
     GET /api/stats/sales-by-category         → [{ categorie, totalVentes }]   (ADMIN)

     GET /api/stats/vendeur/summary           → { totalProducts, totalOrders,
                                                   totalRevenue }               (VENDEUR,
                                                   filtré sur ses propres produits)
     GET /api/stats/vendeur/revenue-monthly   → idem, filtré vendeur

   Si ces endpoints n'existent pas encore côté Spring Boot, les fonctions
   ci-dessous calculent les statistiques EN LOCAL à partir des commandes
   et produits déjà chargés — c'est un calcul de secours, pas une référence
   métier définitive (le calcul serveur doit rester la source de vérité,
   notamment pour les revenus qui impliquent de l'argent réel).
   ============================================================= */

async function getSummary(localData = null) {
  const { data, error } = await GET("/stats/summary");
  if (!error && data) {
    return { products: data.totalProducts, orders: data.totalOrders, users: data.totalUsers, revenue: data.totalRevenue, source: "api" };
  }
  if (!localData) return { products: 0, orders: 0, users: 0, revenue: 0, source: "empty" };
  const { orders, products, users } = localData;
  const revenue = orders.filter(o => o.statut !== "ANNULEE").reduce((s, o) => s + (o.montantTotal || 0), 0);
  const activeProducts = products.filter(p => p.statut !== "SUPPRIME");
  return { products: activeProducts.length, orders: orders.length, users: users.length, revenue, source: "local" };
}

async function getVendeurSummary(localData = null) {
  const { data, error } = await GET("/stats/vendeur/summary");
  if (!error && data) {
    return { products: data.totalProducts, orders: data.totalOrders, revenue: data.totalRevenue, source: "api" };
  }
  if (!localData) return { products: 0, orders: 0, revenue: 0, source: "empty" };
  const { orders, products } = localData;
  const revenue = orders.filter(o => o.statut !== "ANNULEE").reduce((s, o) => s + (o.montantTotal || 0), 0);
  return { products: products.length, orders: orders.length, revenue, source: "local" };
}

const MONTH_LABELS = ["Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"];

function buildMonthlyArray(rawData) {
  const map = {};
  rawData.forEach(d => { map[d.mois] = d.montant; });
  return MONTH_LABELS.map((label, i) => ({ mois: i + 1, label, montant: map[i + 1] || 0 }));
}

async function getRevenueMonthly(localOrders = [], year = new Date().getFullYear(), endpoint = "/stats/revenue-monthly") {
  const { data, error } = await GET(`${endpoint}?year=${year}`);
  if (!error && data && Array.isArray(data)) {
    return buildMonthlyArray(data.map(d => ({ mois: d.mois, montant: d.montant })));
  }
  const byMonth = {};
  localOrders.forEach(order => {
    if (order.statut === "ANNULEE") return;
    const date = new Date(order.dateCommande || order.date || Date.now());
    if (date.getFullYear() !== year) return;
    const mois = date.getMonth() + 1;
    byMonth[mois] = (byMonth[mois] || 0) + (order.montantTotal || 0);
  });
  return buildMonthlyArray(Object.entries(byMonth).map(([mois, montant]) => ({ mois: parseInt(mois), montant })));
}

const CAT_COLORS = {
  "Chaussures":"#2D1B69","Électronique":"#4A30A0","Vêtements":"#F5A623",
  "Accessoires":"#27AE60","Sport":"#E74C3C","Maison":"#2980B9",
};
const FALLBACK_COLORS = ["#7B5CDA","#F39C12","#1ABC9C","#E67E22","#9B59B6"];

function enrichWithColors(items) {
  const total = items.reduce((s,x) => s + x.ventes, 0) || 1;
  return items.map((item, i) => ({
    ...item,
    couleur: CAT_COLORS[item.categorie] || FALLBACK_COLORS[i % FALLBACK_COLORS.length],
    pourcentage: item.pourcentage ?? Math.round((item.ventes / total) * 100)
  }));
}

async function getSalesByCategory(localOrders = [], localProducts = []) {
  const { data, error } = await GET("/stats/sales-by-category");
  if (!error && data && Array.isArray(data)) {
    return enrichWithColors(data.map(d => ({ categorie: d.categorie, ventes: d.totalVentes })));
  }
  const catCount = {};
  localOrders.forEach(order => {
    if (order.statut === "ANNULEE") return;
    if (Array.isArray(order.items)) {
      order.items.forEach(item => {
        const prod = localProducts.find(p => p.id === item.produitId);
        if (prod) catCount[prod.categorie] = (catCount[prod.categorie] || 0) + (item.quantite || 1);
      });
    } else {
      localProducts.forEach(prod => { catCount[prod.categorie] = (catCount[prod.categorie] || 0) + 1; });
    }
  });
  const result = Object.entries(catCount).map(([categorie, ventes]) => ({ categorie, ventes })).sort((a,b) => b.ventes - a.ventes);
  return enrichWithColors(result);
}
