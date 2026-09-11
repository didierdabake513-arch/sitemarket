# TogoShop Frontend — Architecture HTML/CSS/JS

Structure organisée comme un projet React (composants, services, utils séparés)
mais en HTML/CSS/JS vanilla pur, sans framework.

---

## Arborescence

```
togoshop/
├── src/
│   ├── styles/
│   │   ├── base.css          ← Reset, typo, grille, helpers
│   │   ├── components.css    ← Card, table, form, badge, toast, sidebar, modal
│   │   └── buttons.css       ← TOUS les boutons commentés par page
│   │
│   ├── services/
│   │   ├── api.js            ← Client HTTP central (fetch → Spring Boot)
│   │   ├── authService.js    ← login / register / logout
│   │   ├── productService.js ← CRUD produits + orderService + userService
│   │   └── statsService.js   ← Logique réelle revenus + ventes/catégorie
│   │
│   ├── utils/
│   │   ├── charts.js         ← Graphiques SVG natifs (barres + donut)
│   │   └── toast.js          ← Toast + router SPA + validator
│   │
│   └── pages/
│       ├── auth/
│       │   └── Auth.html     ← Connexion / Inscription
│       ├── profil/
│       │   └── Profil.html   ← Profil utilisateur (4 onglets)
│       └── admin/
│           └── Dashboard.html ← Dashboard admin (4 sections)
```

---

## Connexion au backend Spring Boot

### 1. Configurer l'URL de base
Dans `src/services/api.js`, ligne 8 :
```js
const API_BASE_URL = "http://localhost:8080/api";
```
Remplacer par l'URL de production si nécessaire.

### 2. Endpoints attendus

#### Auth
| Méthode | Endpoint | Body | Réponse |
|---------|----------|------|---------|
| POST | `/api/auth/login` | `{ email, password }` | `{ token, user }` |
| POST | `/api/auth/register` | `{ nom, prenom, email, telephone, password }` | `{ token, user }` |
| POST | `/api/auth/logout` | — | 204 |

#### Produits
| Méthode | Endpoint | Corps / Params | Réponse |
|---------|----------|----------------|---------|
| GET | `/api/produits` | — | `[{ id, nom, prix, stock, categorie, emoji, description, prixBarre, enPromotion }]` |
| POST | `/api/produits` | produit | produit créé |
| PUT | `/api/produits/{id}` | produit | produit mis à jour |
| DELETE | `/api/produits/{id}` | — | 204 |

#### Commandes
| Méthode | Endpoint | Corps | Réponse |
|---------|----------|-------|---------|
| GET | `/api/commandes` | — | `[{ id, reference, clientNom, clientId, montantTotal, statut, dateCommande, nbArticles, items[] }]` |
| GET | `/api/commandes/me` | — | commandes de l'utilisateur connecté |
| PUT | `/api/commandes/{id}/statut` | `{ statut }` | commande mise à jour |

**Valeurs de `statut` :** `EN_ATTENTE` · `EXPEDIEE` · `LIVREE` · `ANNULEE`

#### Utilisateurs
| Méthode | Endpoint | Corps | Réponse |
|---------|----------|-------|---------|
| GET | `/api/utilisateurs` | — | liste |
| GET | `/api/utilisateurs/me` | — | profil connecté |
| PUT | `/api/utilisateurs/me` | profil | profil mis à jour |
| PUT | `/api/utilisateurs/me/password` | `{ oldPassword, newPassword }` | 204 |
| PUT | `/api/utilisateurs/me/notifications` | `{ preferences[] }` | 204 |
| PUT | `/api/utilisateurs/{id}/bloquer` | `{ bloquer: bool }` | 204 |
| DELETE | `/api/utilisateurs/{id}` | — | 204 |

#### Stats (dashboard admin)
| Méthode | Endpoint | Réponse |
|---------|----------|---------|
| GET | `/api/stats/summary` | `{ totalProducts, totalOrders, totalUsers, totalRevenue }` |
| GET | `/api/stats/revenue-monthly?year=2025` | `[{ mois: 1, montant: 50000 }, ...]` |
| GET | `/api/stats/sales-by-category` | `[{ categorie, totalVentes }]` |

> **Si ces endpoints n'existent pas encore**, `statsService.js` calcule
> automatiquement les statistiques depuis les données `/api/commandes` et `/api/produits`.

---

## Format JWT attendu

Spring Boot doit retourner un token JWT à la connexion :
```json
{
  "token": "eyJhbGci...",
  "user": {
    "id": 1,
    "nom": "Mensah",
    "prenom": "Kofi",
    "email": "kofi@email.com",
    "role": "CLIENT"
  }
}
```
Le token est stocké dans `localStorage` sous la clé `togoshop_token`
et envoyé automatiquement dans l'en-tête `Authorization: Bearer <token>`
pour toutes les requêtes authentifiées.

---

## Mode hors ligne (fallback)

Quand Spring Boot n'est pas démarré, `api.js` détecte l'erreur réseau (`NETWORK_ERROR`)
et chaque service bascule sur un mode dégradé :
- **Auth** → crée une session locale fictive (sans token réel)
- **Stats** → calculées à partir des données déjà chargées en `STATE`
- **Graphiques** → affichent un état vide élégant (pas de crash)

---

## Identifiants de démo

| Email | Mot de passe | Rôle | Redirection |
|-------|-------------|------|-------------|
| `admin@togoshop.tg` | `admin` | ADMIN | Dashboard.html |
| tout autre email | tout mdp | CLIENT | Profil.html |

---

## Couleurs du projet (sans :root)

| Usage | Valeur |
|-------|--------|
| Primaire | `#2D1B69` |
| Primaire clair | `#4A30A0` |
| Primaire pâle | `#F0EEFB` |
| Or | `#F5A623` |
| Fond global | `#F2F0FB` |
| Texte | `#1A1A2E` |
| Texte atténué | `#7070A0` |
| Bordure | `#DDD9F5` |
| Succès | `#27AE60` |
| Danger | `#E74C3C` |
| Avertissement | `#F39C12` |

---

## Ordre de chargement des scripts

### Auth.html
```html
<script src="../../services/api.js"></script>
<script src="../../services/authService.js"></script>
<script src="../../utils/toast.js"></script>
```

### Profil.html
```html
<script src="../../services/api.js"></script>
<script src="../../services/authService.js"></script>
<script src="../../services/productService.js"></script>  <!-- contient aussi orderService + userService -->
<script src="../../utils/toast.js"></script>              <!-- contient aussi validator + router -->
```

### Dashboard.html
```html
<script src="../../services/api.js"></script>
<script src="../../services/authService.js"></script>
<script src="../../services/productService.js"></script>
<script src="../../utils/toast.js"></script>
<script src="../../utils/charts.js"></script>
<script src="../../services/statsService.js"></script>   <!-- en dernier : dépend de api.js -->
```
