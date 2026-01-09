# Roadmap : Publisher Service (Java)
**Projet JVM-Data 2025**

## 🎯 Objectif du Service
Simuler le comportement d'un éditeur de jeux vidéo qui :
1.  Gère son catalogue de jeux (initialisé via le dataset VGSales).
2.  Publie des mises à jour (Patchs) et des modifications de métadonnées.
3.  Analyse les retours techniques (Crashs) et les agrégats de qualité (Ratings).

---

## 📅 Phase 1 : Initialisation & Structure (Jours 1-2)
*L'objectif est d'avoir un projet qui compile et une base de données locale.*

- [ ] **Création du module Java**
    - [cite_start]Créer le dossier `publisher-service-java` dans le monorepo[cite: 185].
    - Initialiser avec Maven ou Gradle (Java 17 ou 21 recommandé).
    - Ajouter les dépendances :
        - `kafka-clients`
        - `kafka-avro-serializer`
        - `spring-boot-starter-data-jpa` (ou Hibernate pur)
        - Driver BDD (PostgreSQL ou MySQL).
- [ ] **Connexion au module commun**
    - [cite_start]S'assurer que le service dépend du module `common-avro-schemas` pour récupérer les objets Avro générés[cite: 199].
- [ ] **Configuration Docker**
    - [cite_start]Vérifier que le `docker-compose.yml` racine lance bien ta base de données (ex: `publisher-db`)[cite: 179].
- [ ] **Configuration Application**
    - Configurer `application.properties` (ou `.yml`) :
        - URL connexion BDD.
        - Bootstrap servers Kafka.
        - URL Schema Registry.

## 💾 Phase 2 : Modélisation des Données (Jours 3-4)
*Définir comment les données sont stockées et échangées.*

- [ ] **Définition des Schémas Avro (dans `common-avro-schemas`)**
    - Collaborer avec l'équipe pour valider les événements que tu vas **produire** :
        - [cite_start]`GamePatchedEvent` (jeu, version, changelog)[cite: 75].
        - [cite_start]`GameMetadataUpdatedEvent`[cite: 76].
    - Collaborer pour valider les événements que tu vas **consommer** :
        - [cite_start]`GameCrashReportedEvent`[cite: 78].
        - [cite_start]`GameRatingAggregatedEvent`[cite: 79].
- [ ] **Modèle de données relationnel (JPA/SQL)**
    - [cite_start]Créer les entités Java pour ta BDD locale[cite: 83]:
        - `Game` (id, titre, genre, platform, current_version).
        - `CrashReport` (id, game_id, error_code, timestamp).
        - `PatchHistory` (id, game_id, version, date).
        - (Optionnel) `ReviewStats` (pour stocker les agrégats reçus).

## 🚀 Phase 3 : Injection des Données VGSales (Jour 5)
*Le Publisher doit posséder des jeux pour pouvoir les patcher.*

- [ ] **Script d'import VGSales**
    - [cite_start]Télécharger le dataset `vgsales.csv`[cite: 326].
    - Créer un service (`VGSalesLoader`) qui lit le CSV au démarrage.
    - [cite_start]Mapper les colonnes (Name, Platform, Genre, Publisher) vers ton entité `Game`[cite: 329].
    - Sauvegarder ces jeux dans ta BDD locale (`publisher-db`).
    - *Note :* Filtrer pour ne garder que les jeux de l'éditeur "Nintendo" ou "Activision" par exemple, pour simuler un éditeur spécifique, ou tout importer si tu es l'éditeur "Global".

## 📡 Phase 4 : Implémentation Kafka Consumers (Jours 6-7)
*Écouter ce qui se passe sur la plateforme.*

- [ ] **Consumer : Rapports de Crashs**
    - [cite_start]Créer un `KafkaConsumer` pour le topic `game-crash-reported`[cite: 28].
    - Action : À chaque réception, enregistrer le crash en BDD dans la table `crash_reports`.
    - *Logique métier :* Si un jeu dépasse X crashs, logger une alerte "URGENT PATCH NEEDED".
- [ ] **Consumer : Analytics**
    - [cite_start]Créer un `KafkaConsumer` pour le topic `game-rating-aggregated` (venant du service Kotlin)[cite: 79].
    - Action : Mettre à jour les stats du jeu en BDD ou logger la tendance ("Le jeu X a une moyenne de 4.5/5").

## 📢 Phase 5 : Implémentation Kafka Producers (Jours 8-9)
*Agir sur le monde.*

- [ ] **Producer : Publication de Patch**
    - Créer un service `PatchService`.
    - Méthode `deployPatch(String gameId, String version, String content)`.
    - Action 1 : Mettre à jour la version du jeu en BDD et ajouter une entrée dans `PatchHistory`.
    - [cite_start]Action 2 : Produire un événement Avro `GamePatchedEvent` dans le topic `game-patched`[cite: 21].
- [ ] **Producer : Mise à jour Métadonnées**
    - Méthode `updateGameDetails(...)`.
    - [cite_start]Produire un événement sur `game-metadata-updated`[cite: 22].

## 🎮 Phase 6 : API & Simulation (Jours 10-11)
*Rendre le service utilisable.*

- [ ] **Interface de Contrôle (REST API simple)**
    - Exposer des endpoints pour déclencher tes actions :
        - `POST /api/games/{id}/patch` : Déclenche l'envoi d'un patch.
        - `GET /api/reports/crashes` : Affiche les crashs reçus.
- [ ] **Générateur Automatique (Optionnel mais recommandé)**
    - Créer un `ScheduledTask` qui, toutes les X minutes, sélectionne un jeu au hasard dans la BDD et publie un patch mineur (pour générer du trafic Kafka automatiquement pendant la démo).

## 📝 Phase 7 : Documentation & Tests (Jours 12-13)
- [ ] **Tests Unitaires** : Tester la sérialisation Avro et la logique JPA.
- [ ] [cite_start]**Rapport** : Rédiger la partie "Architecture Publisher" pour le rendu final[cite: 355].
- [ ] **Schéma** : Générer un schéma de ta BDD et de tes flux Kafka.

---

## 🛠 Résumé Technique
* **Langage** : Java
* **Type d'app** : Spring Boot (recommandé) ou Java pur.
* **BDD** : PostgreSQL (via Docker).
* **Kafka Topics (Input)** : `game-crash-reported`, `game-rating-aggregated`, `game-crash-stats`.
* **Kafka Topics (Output)** : `game-patched`, `game-metadata-updated`.
* **Source de données** : Fichier `vgsales.csv`.