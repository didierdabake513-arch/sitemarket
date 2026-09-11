# TogoShop Frontend — v2 (architecture séparée, avec espace Vendeur)

Architecture **séparée** comme demandé : CSS, JS et pages HTML sont dans des
dossiers distincts. Testé avec un serveur local (`python -m http.server` ou
Live Server) — tous les chemins relatifs ont été vérifiés un par un.

⚠️ Cette architecture suppose un serveur local. Si vous ouvrez un fichier HTML
directement (double-clic, `file://`), les imports CSS/JS externes ne se
chargeront pas dans certains navigateurs. Utilisez toujours un serveur local.

---

## Arborescence

```
togoshop-v2/
├── README.md
├── styles/
│   ├── base.css          ← reset, typo, helpers
│   ├── components.css    ← navbar, footer, card, table, badge, sidebar, modal
│   └── buttons.css       ← TOUS les boutons, commentés par page d'usage
├── services/
│   ├── api.js             ← client HTTP central (charger EN PREMIER sur chaque page)
│   ├── authService.js     ← login, register, logout, devenir vendeur
│   ├── productService.js  ← CRUD produits (+ "mes produits" pour le vendeur)
│   ├── orderService.js    ← commandes (+ "commandes vendeur" filtrées)
│   ├── userService.js     ← profil, sécurité, gestion admin des utilisateurs
│   └── statsService.js    ← stats admin ET vendeur (calcul réel, pas mocké)
├── utils/
│   ├── toast.js           ← toast + validator (force mdp, emails) + panier (localStorage)
│   └── charts.js          ← graphiques SVG natifs (barres + donut), réutilisés admin/vendeur
└── pages/
    ├── home/Accueil.html
    ├── catalogue/Catalogue.html
    ├── produit/Produit.html
    ├── panier/Panier.html
    ├── commande/Commande.html
    ├── auth/Auth.html
    ├── profil/Profil.html
    ├── vendeur/DashboardVendeur.html   ← NOUVEAU
    └── admin/Dashboard.html
```

---

## Comment tester

```bash
cd togoshop-v2
python3 -m http.server 8000
```
Puis ouvrir `http://localhost:8000/pages/home/Accueil.html`.

Avec VS Code + extension Live Server : clic droit sur `pages/home/Accueil.html` → "Open with Live Server".

---

## Logique des rôles (important)

| Rôle | Comment on l'obtient | Accès |
|------|----------------------|-------|
| **Visiteur (non connecté)** | Par défaut | Catalogue, détail produit, panier (local), estimation livraison **générique** |
| **CLIENT** | Inscription libre via `Auth.html` (seul rôle proposé au formulaire) | Tout ce que le visiteur a + historique commandes, profil, estimation livraison **précise** (basée sur son adresse), notifications personnalisées |
| **VENDEUR** | Un CLIENT clique sur **"Devenir vendeur"** dans `Profil.html` → activation immédiate | Tout ce que CLIENT a + `DashboardVendeur.html` (gestion de ses propres produits, suivi de ses commandes reçues) |
| **ADMIN** | **Jamais auto-inscrit.** Un admin existant envoie une invitation par email depuis `Dashboard.html` → section "Invitations admin". L'élévation de rôle se fait uniquement quand la personne clique sur le lien reçu | `Dashboard.html` complet (tous produits, toutes commandes, tous utilisateurs, invitations) |

**Le formulaire d'inscription ne propose jamais de choisir CLIENT/VENDEUR/ADMIN.**
Tout compte créé via `Auth.html` est CLIENT par défaut, côté serveur.

---

## ⚙️ Ce qui doit être codé côté Spring Boot

Le front ne fait **que des appels `fetch()`**. Toute la logique métier réelle
(vérification mot de passe, calcul de prix, changement de rôle, envoi d'email,
filtrage des données par utilisateur) doit exister dans vos `Controller` /
`Service` / `Repository` / `Entity`.

### Auth
| Méthode | Endpoint | Body | Réponse |
|---|---|---|---|
| POST | `/api/auth/login` | `{ email, password }` | `{ token, user }` |
| POST | `/api/auth/register` | `{ nom, prenom, email, telephone, password }` | `{ token, user }` — **rôle CLIENT forcé côté serveur** |
| POST | `/api/auth/logout` | — | 204 |
| PUT | `/api/utilisateurs/me/devenir-vendeur` | — | `{ user }` — change le rôle CLIENT → VENDEUR de l'utilisateur authentifié (jamais un autre) |

### Produits
| Méthode | Endpoint | Réponse |
|---|---|---|
| GET | `/api/produits` | tous les produits (public) |
| GET | `/api/produits/{id}` | un produit |
| GET | `/api/produits/me` | **uniquement** les produits du VENDEUR connecté (filtre par vendeurId côté serveur, jamais côté front) |
| POST | `/api/produits` | créer — si rôle VENDEUR, associer automatiquement `vendeurId` = utilisateur courant |
| PUT/DELETE | `/api/produits/{id}` | vérifier que le vendeur ne modifie/supprime que SES produits (sauf ADMIN) |

### Commandes
| Méthode | Endpoint | Body | Réponse |
|---|---|---|---|
| POST | `/api/commandes` | `{ adresseLivraison, telephone, modePaiement, items[], montantTotal }` | `{ reference }` — **recalculer le total côté serveur**, ne jamais faire confiance au total envoyé par le front |
| GET | `/api/commandes/me` | — | commandes du CLIENT connecté |
| GET | `/api/commandes` | — | toutes les commandes (ADMIN) |
| GET | `/api/commandes/vendeur/me` | — | commandes contenant ≥1 produit du VENDEUR connecté |
| PUT | `/api/commandes/{id}/statut` | `{ statut }` | ADMIN, ou VENDEUR sur ses propres commandes uniquement |

### Utilisateurs
| Méthode | Endpoint | Body |
|---|---|---|
| GET/PUT | `/api/utilisateurs/me` | profil |
| PUT | `/api/utilisateurs/me/password` | `{ oldPassword, newPassword }` |
| PUT | `/api/utilisateurs/me/notifications` | `{ preferences[] }` |
| DELETE | `/api/utilisateurs/me` | — |
| GET | `/api/utilisateurs` | tous (ADMIN) |
| PUT | `/api/utilisateurs/{id}/bloquer` | `{ bloquer }` (ADMIN) |
| DELETE | `/api/utilisateurs/{id}` | (ADMIN) |

### Invitations admin
| Méthode | Endpoint | Body | Logique back attendue |
|---|---|---|---|
| POST | `/api/admin/invitations` | `{ email }` | Générer un token unique + expiration, l'enregistrer en base, envoyer un email avec un lien `https://votresite/activation-admin?token=XXX`. Un endpoint séparé doit élever le rôle en ADMIN quand ce lien est visité par un utilisateur connecté. |

### Statistiques (idéalement calculées côté serveur)
| Méthode | Endpoint | Réponse |
|---|---|---|
| GET | `/api/stats/summary` | `{ totalProducts, totalOrders, totalUsers, totalRevenue }` (ADMIN, vue globale) |
| GET | `/api/stats/revenue-monthly?year=2025` | `[{ mois, montant }]` (ADMIN) |
| GET | `/api/stats/sales-by-category` | `[{ categorie, totalVentes }]` (ADMIN) |
| GET | `/api/stats/vendeur/summary` | mêmes champs mais **filtrés** sur les produits du vendeur connecté |
| GET | `/api/stats/vendeur/revenue-monthly` | idem, filtré vendeur |

Si ces endpoints stats n'existent pas encore, `statsService.js` calcule un
résultat de secours à partir des commandes/produits déjà chargés — pratique
pour développer le front en attendant, mais **pas une source fiable pour de
l'argent réel** : le calcul serveur doit rester la référence définitive.

---

## Fonctionnalité "non connecté vs connecté" (livraison)

Implémentée pour l'instant uniquement dans `Produit.html` (affichage différent
d'une estimation de livraison). Concrètement :
- **Non connecté** : message générique ("2 à 5 jours selon votre zone")
- **Connecté** : message basé sur l'adresse enregistrée dans le profil

⚙️ Pour aller plus loin, créer côté Spring Boot un endpoint du type
`GET /api/utilisateurs/me/estimation-livraison?produitId=X` qui calcule un
vrai délai à partir de l'adresse réelle et du stock/localisation du vendeur.
Le calcul de distance, les emails de confirmation personnalisés, et les
recommandations de produits sont entièrement à faire côté backend — le front
ne fait qu'afficher ce que l'API renverra.

---

## Identifiants de démo (mode hors ligne uniquement)

| Email | Mot de passe | Rôle simulé |
|---|---|---|
| `admin@togoshop.tg` | `admin` | ADMIN |
| tout autre email | tout mot de passe | CLIENT |

Le passage à VENDEUR se fait ensuite depuis `Profil.html` → bouton "Devenir vendeur".

---

## Charte graphique (valeurs directes, pas de `:root`)

| Usage | Valeur |
|---|---|
| Primaire (clients/admin) | `#2D1B69` |
| Primaire clair | `#4A30A0` |
| Vendeur (sidebar) | `#1E8449` → `#196F3D` |
| Or / accent | `#F5A623` |
| Fond | `#F2F0FB` / `#FAFAF8` |
| Texte | `#1A1A2E` |
| Texte atténué | `#7070A0` |
| Succès | `#27AE60` |
| Danger | `#E74C3C` |
| Avertissement | `#F39C12` |

Polices : **Sora** (titres), **Inter** (texte), via Google Fonts.
