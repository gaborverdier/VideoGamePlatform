-- Script d'initialisation pour PostgreSQL
-- Ce script crée la base de données et l'utilisateur pour le Platform Service

-- La base de données est déjà créée par POSTGRES_DB dans docker-compose
-- L'utilisateur est déjà créé par POSTGRES_USER dans docker-compose

-- Vérification de la connexion
SELECT 'PostgreSQL est prêt pour Platform Service!' as status;

-- Les tables seront créées automatiquement par Hibernate avec spring.jpa.hibernate.ddl-auto=update
