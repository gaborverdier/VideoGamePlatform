-- Création des bases et utilisateurs pour chaque service
CREATE DATABASE videogames_db;
CREATE DATABASE publisher_db;

-- Utilisateur pour videogames_db
CREATE USER videogames_user WITH ENCRYPTED PASSWORD 'secretpassword';
GRANT ALL PRIVILEGES ON DATABASE videogames_db TO videogames_user;
\c videogames_db
GRANT ALL PRIVILEGES ON SCHEMA public TO videogames_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO videogames_user;

-- Utilisateur pour publisher_db
CREATE USER publisher_user WITH ENCRYPTED PASSWORD 'publisher_password';
GRANT ALL PRIVILEGES ON DATABASE publisher_db TO publisher_user;
\c publisher_db
GRANT ALL PRIVILEGES ON SCHEMA public TO publisher_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO publisher_user;
