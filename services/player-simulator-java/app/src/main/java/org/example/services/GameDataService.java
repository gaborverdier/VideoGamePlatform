package org.example.services;

import org.example.models.Game;
import org.example.models.Review;

import java.util.ArrayList;
import java.util.List;

public class GameDataService {
    private static GameDataService instance;
    private List<Game> allGames;
    
    private GameDataService() {
        this.allGames = new ArrayList<>();
        loadMockGames();
    }
    
    public static GameDataService getInstance() {
        if (instance == null) {
            instance = new GameDataService();
        }
        return instance;
    }
    
    private void loadMockGames() {
        Game eldenRing = new Game("Elden Ring", 59.99, "RPG",
            "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1245620/header.jpg",
            "Un RPG d'action en monde ouvert dans les Terres Intermédiaires.  Explorez un vaste monde et affrontez des boss épiques.",
            4.8, 80);
        eldenRing.addUpdate("Patch 1.12 - Corrections de bugs");
        eldenRing.addUpdate("Patch 1.13 - Améliorations performances");
        eldenRing.addReview(new Review(eldenRing.getId(), "P1", "Jean", 5, "Chef d'œuvre absolu !  Les boss sont incroyables.", 120));
        eldenRing. addReview(new Review(eldenRing.getId(), "P2", "Marie", 4, "Très bon mais difficile pour les débutants.", 45));
        allGames.add(eldenRing);
        
        Game minecraft = new Game("Minecraft", 29.99, "Sandbox",
            "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1276930/header.jpg",
            "Jeu de construction et d'aventure en monde ouvert. Créez, explorez et survivez dans un univers de blocs.",
            4.5, 200);
        minecraft.addUpdate("Update 1.20 - Trails & Tales");
        minecraft.addReview(new Review(minecraft.getId(), "P3", "Lucas", 5, "Infini possibilités créatives !", 500));
        allGames.add(minecraft);
        
        Game fifa = new Game("FIFA 24", 69.99, "Sport",
            "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/2195250/header.jpg",
            "Le jeu de football le plus réaliste.  Jouez avec vos équipes favorites et participez à des tournois.",
            4.0, 50);
        fifa.addReview(new Review(fifa.getId(), "P4", "Sophie", 3, "Pas assez de changements par rapport à FIFA 23.", 30));
        allGames.add(fifa);
        
        Game witcher = new Game("The Witcher 3", 39.99, "RPG",
            "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/292030/header.jpg",
            "Incarnez Geralt de Riv et partez dans une quête épique à travers un monde fantastique immense.",
            4.9, 120);

        witcher.addReview(new Review(witcher.getId(), "P5", "Thomas", 5, "L'un des meilleurs RPG de tous les temps.", 200));
        witcher.addReview(new Review(witcher.getId(), "P6", "Emma", 5, "Histoire captivante, graphismes magnifiques.", 150));
        allGames.add(witcher);
        
        Game gta = new Game("GTA V", 29.99, "Action",
            "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/271590/header.jpg",
            "Explorez Los Santos dans ce jeu d'action en monde ouvert. Missions, courses et chaos urbain vous attendent.",
            4.7, 100);
        gta.addReview(new Review(gta. getId(), "P7", "Alex", 5, "Toujours aussi fun après toutes ces années !", 300));
        allGames.add(gta);
        
        Game cyberpunk = new Game("Cyberpunk 2077", 49.99, "RPG",
            "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1091500/header.jpg",
            "RPG futuriste dans la mégalopole de Night City. Personnalisez votre personnage et tracez votre chemin.",
            4.2, 60);
        cyberpunk.addUpdate("Patch 2.0 - Refonte complète");
        cyberpunk. addReview(new Review(cyberpunk.getId(), "P8", "Nina", 4, "Beaucoup mieux depuis les mises à jour !", 80));
        allGames.add(cyberpunk);
        
        Game csgo = new Game("CS:GO", 0.00, "FPS",
            "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg",
            "Jeu de tir tactique en équipe. Affrontez des adversaires dans des modes compétitifs.",
            4.6, 300);
        csgo.addReview(new Review(csgo. getId(), "P9", "Kevin", 5, "Le meilleur FPS compétitif !", 1000));
        allGames.add(csgo);
        
        Game valorant = new Game("Valorant", 0.00, "FPS",
            "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg",
            "FPS tactique 5v5 avec des agents aux capacités uniques. Stratégie et précision sont la clé.",
            4.4, 150);
        valorant.addReview(new Review(valorant. getId(), "P10", "Laura", 4, "Très bon mélange entre CS et Overwatch.", 250));
        allGames.add(valorant);

        eldenRing.addDLC("Shadow of the Erdtree", 39.99);

        minecraft.addDLC("Marketplace Pass", 9.99);

        witcher.addDLC("Hearts of Stone", 9.99);
        witcher.addDLC("Blood and Wine", 19.99);

        cyberpunk.addDLC("Phantom Liberty", 29.99);
    }
    
    public List<Game> getAllGames() {
        return new ArrayList<>(allGames);
    }
    
    public Game findGameById(String id) {
        return allGames.stream()
            .filter(g -> g.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}