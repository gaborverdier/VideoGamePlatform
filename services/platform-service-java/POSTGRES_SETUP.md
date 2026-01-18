# Configuration PostgreSQL pour Platform Service

## Configuration Docker

PostgreSQL est configuré dans `docker-compose.yml` avec les paramètres suivants :

- **Image** : `postgres:16`
- **Port** : `5432` (mappé sur l'hôte)
- **Base de données** : `videogames_db`
- **Utilisateur** : `videogames_user`
- **Mot de passe** : `secretpassword`

## Démarrage

```bash
cd docker
docker-compose up -d postgres
```

## Vérification

### Vérifier que PostgreSQL est prêt
```bash
docker exec postgres pg_isready -U videogames_user -d videogames_db
```

### Se connecter à PostgreSQL
```bash
docker exec -it postgres psql -U videogames_user -d videogames_db
```

### Commandes utiles dans psql
```sql
-- Lister les tables
\dt

-- Lister les bases de données
\l

-- Décrire une table
\d platform_users

-- Quitter
\q
```

## PgAdmin

PgAdmin est disponible sur http://localhost:5050

**Connexion** :
- Email : `admin@local.com`
- Mot de passe : `admin`

**Configuration du serveur PostgreSQL dans PgAdmin** :
1. Clic droit sur "Servers" → "Register" → "Server"
2. Onglet "General" :
   - Name : `Platform DB`
3. Onglet "Connection" :
   - Host : `postgres` (nom du conteneur dans le réseau Docker)
   - Port : `5432`
   - Maintenance database : `videogames_db`
   - Username : `videogames_user`
   - Password : `secretpassword`
   - Save password : Oui

## Configuration Spring Boot

Le fichier `application.properties` est configuré pour PostgreSQL :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/videogames_db
spring.datasource.username=videogames_user
spring.datasource.password=secretpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Mode DDL : `update`

Le mode `update` permet à Hibernate de :
- ✅ Créer les tables si elles n'existent pas
- ✅ Ajouter de nouvelles colonnes
- ✅ Modifier les types de colonnes (avec précautions)
- ❌ **Ne supprime jamais** de tables ou colonnes

**Avantages** :
- Développement rapide sans perte de données
- Évolution progressive du schéma

**Pour la production** : utiliser Flyway ou Liquibase pour les migrations versionnées.

## Tables créées automatiquement

Au démarrage du Platform Service, Hibernate crée automatiquement :

1. **platform_users**
   - Stockage des utilisateurs enregistrés
   - Index sur `username` et `email`

2. **platform_games**
   - Catalogue des jeux disponibles
   - Index sur `title` et `publisher`

3. **platform_purchases**
   - Historique des achats
   - Index sur `user_id`, `game_id`, et `purchase_date`

## Commandes utiles

### Redémarrer PostgreSQL
```bash
docker-compose restart postgres
```

### Voir les logs PostgreSQL
```bash
docker logs postgres -f
```

### Nettoyer la base (ATTENTION : supprime toutes les données)
```bash
docker-compose down -v
docker-compose up -d postgres
```

### Backup de la base
```bash
docker exec postgres pg_dump -U videogames_user videogames_db > backup.sql
```

### Restaurer la base
```bash
cat backup.sql | docker exec -i postgres psql -U videogames_user -d videogames_db
```

## Basculer entre H2 et PostgreSQL

Pour revenir à H2 (base en mémoire), commentez les lignes PostgreSQL dans `application.properties` et décommentez les lignes H2 :

```properties
# PostgreSQL (commenté)
# spring.datasource.url=jdbc:postgresql://localhost:5432/videogames_db
# ...

# H2 (actif)
spring.datasource.url=jdbc:h2:mem:platform_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```

## Troubleshooting

### Erreur : "le rôle « videogames_user » n'existe pas"
→ PostgreSQL n'est pas encore démarré ou configuré
```bash
docker-compose restart postgres
docker exec postgres pg_isready -U videogames_user -d videogames_db
```

### Erreur : "Connection refused"
→ PostgreSQL n'est pas accessible
```bash
docker ps | grep postgres
docker logs postgres
```

### Les tables ne sont pas créées
→ Vérifier que `spring.jpa.hibernate.ddl-auto=update` est bien configuré
→ Vérifier les logs Spring Boot au démarrage
