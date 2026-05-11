#!/bin/bash

# ============================================
# Script de déploiement Simul'Enfance
# ============================================

echo "🚀 Déploiement de Simul'Enfance"
echo "================================"
echo ""

# Vérifier que Docker est lancé
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker n'est pas lancé. Veuillez démarrer Docker Desktop."
    exit 1
fi

echo "✅ Docker est actif"
echo ""

# Se positionner dans le répertoire backend (où se trouve docker-compose.fullstack.yml)
cd "$(dirname "$0")"

# Arrêter les anciens conteneurs (si existants)
echo "🛑 Arrêt des anciens conteneurs..."
docker-compose -f docker-compose.fullstack.yml down

echo ""
echo "🔨 Build et démarrage des services..."
echo "   - PostgreSQL"
echo "   - Backend Spring Boot"
echo "   - Frontend Angular"
echo ""
echo "⏳ Cela peut prendre 5-10 minutes la première fois..."
echo ""

# Build et démarrage de tous les services
docker-compose -f docker-compose.fullstack.yml up -d --build

echo ""
echo "⏳ Attente du démarrage complet des services (60 secondes)..."
sleep 60

echo ""
echo "🏥 Vérification de la santé des services..."
echo ""

# Vérifier PostgreSQL
if docker ps | grep -q "simul_postgres"; then
    echo "✅ PostgreSQL : En ligne"
else
    echo "❌ PostgreSQL : Erreur"
fi

# Vérifier Backend
if docker ps | grep -q "simul_backend"; then
    echo "✅ Backend : En ligne"
else
    echo "❌ Backend : Erreur"
fi

# Vérifier Frontend
if docker ps | grep -q "simul_frontend"; then
    echo "✅ Frontend : En ligne"
else
    echo "❌ Frontend : Erreur"
fi

echo ""
echo "================================================"
echo "🎉 Déploiement terminé !"
echo "================================================"
echo ""
echo "📍 URLs d'accès :"
echo "   Frontend : http://localhost"
echo "   Backend  : http://localhost:8080"
echo "   PostgreSQL : localhost:5432"
echo ""
echo "📋 Commandes utiles :"
echo "   Voir les logs       : docker-compose -f docker-compose.fullstack.yml logs -f"
echo "   Arrêter l'app       : docker-compose -f docker-compose.fullstack.yml down"
echo "   Redémarrer l'app    : docker-compose -f docker-compose.fullstack.yml restart"
echo "   Voir l'état         : docker-compose -f docker-compose.fullstack.yml ps"
echo ""
