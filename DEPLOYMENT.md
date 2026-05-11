# 🚀 Guide de Déploiement - Simul'Enfance

## Prérequis

- ✅ Docker Desktop installé et lancé
- ✅ 4 Go de RAM disponible minimum
- ✅ 10 Go d'espace disque libre

## 🎯 Déploiement Rapide (Méthode Automatique)

### 1. Lancer le script de déploiement

```bash
cd simul-enfance-backend
./deploy.sh
```

**Durée : 5-10 minutes la première fois (téléchargement des images + build)**

### 2. Accéder à l'application

- 🎨 **Frontend** : http://localhost
- 🔧 **Backend API** : http://localhost:8080
- 🗄️ **PostgreSQL** : localhost:5432

### 3. Tester l'application

1. Ouvrir http://localhost dans ton navigateur
2. Créer un compte via "S'inscrire"
3. Te connecter
4. Utiliser le calculateur de tarifs

---

## 🔧 Déploiement Manuel (Méthode Détaillée)

### Étape 1 : Préparer les variables d'environnement

```bash
cd simul-enfance-backend
cp .env.example .env
```

**Modifier le fichier `.env` si besoin :**
```bash
DB_NAME=municipal_tariffs
DB_USER=postgres
DB_PASSWORD=MotDePasseSecurise123  # Changer en production !
JWT_SECRET=VotreCleSecrete256BitsMinimum  # Générer avec : openssl rand -base64 32
```

### Étape 2 : Build des images Docker

```bash
# Depuis le dossier parent qui contient les 2 projets
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml build
```

### Étape 3 : Lancer tous les services

```bash
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml up -d
```

### Étape 4 : Vérifier l'état des conteneurs

```bash
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml ps
```

**Résultat attendu :**
```
NAME              STATUS         PORTS
simul_postgres    Up (healthy)   0.0.0.0:5432->5432/tcp
simul_backend     Up (healthy)   0.0.0.0:8080->8080/tcp
simul_frontend    Up             0.0.0.0:80->80/tcp
```

### Étape 5 : Voir les logs

```bash
# Logs de tous les services
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml logs -f

# Logs du backend uniquement
docker logs -f simul_backend

# Logs du frontend uniquement
docker logs -f simul_frontend
```

---

## 🛠️ Commandes Utiles

### Arrêter l'application

```bash
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml down
```

### Arrêter ET supprimer les données (reset complet)

```bash
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml down -v
```

⚠️ **Attention : Cette commande supprime la base de données !**

### Redémarrer un service spécifique

```bash
# Redémarrer le backend
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml restart backend

# Redémarrer le frontend
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml restart frontend
```

### Rebuild après modification du code

```bash
# Rebuild tous les services
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml up -d --build

# Rebuild seulement le backend
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml up -d --build backend
```

### Se connecter à PostgreSQL

```bash
# Via Docker
docker exec -it simul_postgres psql -U postgres -d municipal_tariffs

# Via psql local (si installé)
psql -h localhost -U postgres -d municipal_tariffs
```

### Voir l'utilisation des ressources

```bash
docker stats
```

---

## 🐛 Dépannage

### Problème : "Port 80 déjà utilisé"

**Solution 1 :** Arrêter le service qui utilise le port 80
```bash
# Voir quel processus utilise le port 80
lsof -i :80

# Arrêter Apache (si installé)
sudo apachectl stop
```

**Solution 2 :** Changer le port dans docker-compose.fullstack.yml
```yaml
frontend:
  ports:
    - "8081:80"  # Utiliser le port 8081 à la place
```

### Problème : "Port 8080 déjà utilisé"

```bash
# Voir quel processus utilise le port 8080
lsof -i :8080

# Tuer le processus (remplacer PID par l'ID du processus)
kill -9 PID
```

### Problème : Backend ne démarre pas (erreur de connexion BDD)

1. Vérifier que PostgreSQL est bien démarré :
```bash
docker logs simul_postgres
```

2. Vérifier les logs du backend :
```bash
docker logs simul_backend
```

3. Redémarrer dans l'ordre :
```bash
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml restart postgres
sleep 10
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml restart backend
```

### Problème : Frontend affiche "Cannot connect to backend"

1. Vérifier que le backend répond :
```bash
curl http://localhost:8080/api/auth/login
```

2. Vérifier l'URL de l'API dans `environment.prod.ts` :
```typescript
apiUrl: 'http://localhost:8080/api'  // Doit être correct
```

3. Rebuild le frontend :
```bash
docker-compose -f simul-enfance-backend/docker-compose.fullstack.yml up -d --build frontend
```

### Problème : "Disk space full"

```bash
# Nettoyer les images inutilisées
docker system prune -a

# Supprimer les volumes non utilisés
docker volume prune
```

---

## 📊 Architecture des Conteneurs

```
┌─────────────────────────────────────────────────┐
│  Docker Network: simul-network                  │
│                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌────────┐│
│  │  Frontend    │  │  Backend     │  │ Postgres││
│  │  (Angular)   │→ │ (Spring Boot)│→ │         ││
│  │  Port: 80    │  │  Port: 8080  │  │ 5432    ││
│  │  Nginx       │  │  Java 17     │  │ PG 16   ││
│  └──────────────┘  └──────────────┘  └────────┘│
└─────────────────────────────────────────────────┘
       ↓                    ↓                ↓
   http://localhost   http://localhost:8080  BDD
```

---

## 🔐 Sécurité en Production

Si tu déploies sur un serveur public :

1. ✅ **Changer les mots de passe** dans `.env`
2. ✅ **Générer un JWT_SECRET fort** : `openssl rand -base64 32`
3. ✅ **Activer HTTPS** avec Let's Encrypt
4. ✅ **Configurer un firewall** (bloquer le port 5432)
5. ✅ **Mettre à jour l'URL de l'API** dans `environment.prod.ts`
6. ✅ **Activer les logs de sécurité**

---

## 📝 Notes

- La première fois, le téléchargement et le build prennent **5-10 minutes**
- Les fois suivantes, le démarrage prend **30-60 secondes**
- Les données PostgreSQL sont **persistées** dans un volume Docker
- Pour un **reset complet**, utiliser `docker-compose down -v`

---

## 🆘 Besoin d'aide ?

Si tu rencontres un problème :
1. Vérifier les logs : `docker-compose logs -f`
2. Vérifier l'état : `docker-compose ps`
3. Redémarrer : `docker-compose restart`

**Bon déploiement ! 🚀**
