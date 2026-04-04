# 🏛️ Simul'Enfance Backend

API REST Spring Boot pour le calcul des tarifs de services périscolaires municipaux.

## 📋 Description

Backend de l'application Simul'Enfance permettant :
- Calcul des tarifs selon le quotient familial (QF)
- Gestion des utilisateurs et authentification JWT
- Gestion des simulations et historique
- Administration des barèmes et configurations tarifaires
- Support multi-villes avec différents systèmes de tarification

## 🚀 Technologies

- **Framework** : Spring Boot 3.5.12
- **Java** : 17
- **Base de données** : PostgreSQL 16
- **Sécurité** : Spring Security + JWT
- **ORM** : Spring Data JPA / Hibernate
- **Build** : Maven
- **Conteneurisation** : Docker (docker-compose)

## 📦 Installation

### Prérequis
- Java 17+
- Maven 3.8+
- Docker & Docker Compose (pour PostgreSQL)

### Étapes

```bash
# Cloner le repository
git clone https://github.com/dainata92/simul-enfance-backend.git
cd simul-enfance-backend

# Démarrer PostgreSQL avec Docker
docker-compose up -d

# Lancer l'application (script automatisé)
./run.sh

# OU manuellement avec Maven
./mvnw spring-boot:run
```

L'API sera accessible sur `http://localhost:8080`

## 🗄️ Configuration Base de données

Le `docker-compose.yml` configure PostgreSQL :
```yaml
Database: simul_enfance
User: postgres
Password: postgres
Port: 5432
```

Profils disponibles :
- `dev` : Base H2 en mémoire (développement)
- `prod` : PostgreSQL (production)

Activer un profil :
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🛠️ Scripts Maven

```bash
./mvnw clean install     # Compiler et packager
./mvnw spring-boot:run   # Lancer l'application
./mvnw test              # Exécuter les tests
./mvnw package           # Créer le JAR exécutable
```

## 📁 Architecture du projet

```
src/main/java/com/municipal/
├── config/               # Configuration (CORS, Web)
├── controller/           # Endpoints REST
│   ├── AuthController           # Login/Register
│   ├── CalculationController    # Calcul tarifs
│   ├── SimulationController     # CRUD simulations
│   ├── UserController           # Gestion utilisateurs
│   └── AdminController          # Administration
├── dto/                  # Data Transfer Objects
├── entity/               # Entités JPA
│   ├── User                     # Utilisateurs
│   ├── Simulation              # Simulations sauvegardées
│   ├── City                    # Villes supportées
│   ├── CalculationRule         # Règles de calcul
│   ├── PriceBracket            # Tranches tarifaires
│   └── TauxEffortConfig        # Config taux d'effort
├── repository/           # Repositories JPA
├── security/             # Sécurité JWT
│   ├── SecurityConfig          # Configuration Spring Security
│   ├── JwtFilter               # Filtre JWT
│   ├── JwtUtil                 # Génération/validation tokens
│   └── UserDetailsServiceImpl  # Chargement utilisateurs
├── service/              # Logique métier
│   ├── PricingService          # Calculs tarifaires
│   ├── AuthService             # Authentification
│   ├── SimulationService       # Gestion simulations
│   └── UserService             # Gestion utilisateurs
└── exception/            # Gestion des erreurs
```

## 🔐 Sécurité

### Authentification JWT

1. **Login** : `POST /api/auth/login`
   ```json
   {
     "email": "user@example.com",
     "password": "password"
   }
   ```
   Retourne un token JWT valide 24h

2. **Utilisation** : Ajouter le header
   ```
   Authorization: Bearer <token>
   ```

### Routes publiques
- `/api/auth/**` - Login, register
- `/api/calculate/**` - Calculateur public
- `/api/cities/**` - Liste des villes

### Routes protégées
- `/api/simulations/**` - Authentification requise
- `/api/user/**` - Authentification requise
- `/api/admin/**` - Rôle ADMIN requis

## 📡 API Endpoints

### Authentification
```
POST   /api/auth/register      # Inscription
POST   /api/auth/login         # Connexion
```

### Calculs
```
POST   /api/calculate          # Calculer tarifs
GET    /api/cities             # Liste villes
GET    /api/pricing/{cityId}   # Config ville
```

### Simulations (authentifié)
```
GET    /api/simulations        # Mes simulations
POST   /api/simulations        # Créer simulation
GET    /api/simulations/{id}   # Détail simulation
DELETE /api/simulations/{id}   # Supprimer
```

### Administration (ADMIN)
```
POST   /api/admin/init-mercredi-perreux      # Init données test
POST   /api/admin/init-restauration-perreux  # Init restauration
GET    /api/admin/users                      # Liste utilisateurs
```

## 🧮 Logique de calcul

Le `PricingService` implémente :
1. **Calcul QF** à partir du revenu fiscal et nombre de parts
2. **Détermination tranche** selon le QF calculé
3. **Application tarif** selon le type de service
4. **Calcul mensuel** basé sur fréquence et calendrier

Types de services supportés :
- Restauration scolaire
- Accueil périscolaire (matin/soir)
- Centres de loisirs (mercredi/vacances)

## 🧪 Tests

```bash
# Tous les tests
./mvnw test

# Tests d'un package spécifique
./mvnw test -Dtest=PricingServiceTest
```

Tests disponibles :
- `PricingServiceTest` - Logique de calcul
- `CalculationControllerTest` - Endpoints API
- `CalculationRuleRepositoryTest` - Accès données

## 🐳 Docker

Démarrer uniquement PostgreSQL :
```bash
docker-compose up -d
```

Arrêter :
```bash
docker-compose down
```

Voir les logs :
```bash
docker-compose logs -f
```

## 🚢 Déploiement

### Build du JAR
```bash
./mvnw clean package
java -jar target/simul-enfance-backend-0.0.1-SNAPSHOT.jar
```

### Variables d'environnement
```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://host:5432/dbname
DATABASE_USERNAME=user
DATABASE_PASSWORD=pass
JWT_SECRET=your-secret-key
```

### Plateformes recommandées
- **Railway** : Déploiement automatique depuis GitHub
- **Heroku** : Support PostgreSQL intégré
- **AWS EC2** : Pour plus de contrôle

## 🔧 Configuration

Fichiers de configuration :
- `application.properties` - Config commune
- `application-dev.properties` - Développement
- `application-prod.properties` - Production

Personnaliser le port :
```properties
server.port=8080
```

## 🔗 Liens

- **Frontend** : [simul-enfance-frontend](https://github.com/dainata92/simul-enfance-frontend)
- **Spring Boot Docs** : https://spring.io/projects/spring-boot

## 👥 Auteur

Développé pour la soutenance - Simul'Enfance

## 📄 Licence

Projet académique
