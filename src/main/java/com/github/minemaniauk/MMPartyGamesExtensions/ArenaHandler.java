package com.github.minemaniauk.MMPartyGamesExtensions;

import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import me.wazup.partygames.Arena;
import me.wazup.partygames.PartyGames;
import me.wazup.partygames.events.PGStartMiniGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class ArenaHandler implements Listener {

    public final String arenaName;
    public final Arena arena;
    public Configuration weightsConfig;
    public Configuration mainConfig;
    public Plugin partygames;
    public int maxMinigameRounds;
    public int playedRounds;
    public List<String> gamelist = new ArrayList<>();
    public String fisrtGame;

    private LinkedHashMap<String, Integer> gamesToBePlayed = new LinkedHashMap<>();

    public ArenaHandler(String arenaName, Plugin pg) {

        this.partygames = pg;
        this.arenaName = arenaName.toLowerCase();
        this.arena = PartyGames.getInstance().arenas.get(arenaName.toLowerCase());
        this.weightsConfig = MMPartyGamesExtensions.getInstance().getGameWeightsConfig();
        this.mainConfig = MMPartyGamesExtensions.getInstance().getMainConfig();
        this.playedRounds=1;
        this.fisrtGame = arena.currentMinigame.getActualName();

        if (this.arena == null) {
            Bukkit.getLogger().warning("Tried the create arena Arena Handler but could not find arena in PG plugin");
            MMPartyGamesExtensions.getInstance().RemoveArenaHandler(arenaName.toLowerCase());
            return;
        }

        for (String game : arena.registeredMinigames) {
            gamesToBePlayed.put(game, weightsConfig.getInteger("game-weights." + game));
        }
        Bukkit.getLogger().warning("minigames = " + gamesToBePlayed);

        List<String> minigamesToPlay = arena.minigamesToPlay;
        for (String game : arena.registeredMinigames) {
            if(!minigamesToPlay.contains(game)) {
                this.fisrtGame = game;
            }
        }

        File confFile = new File(pg.getDataFolder() + "/arenas", arenaName + ".yml");
        YamlConfiguration configFile = YamlConfiguration.loadConfiguration(confFile);
        this.maxMinigameRounds = ((FileConfiguration)configFile).getInt("max-minigame-rounds");
        Bukkit.getLogger().warning("total rounds = " + maxMinigameRounds);


        SortGames();
    }

    public void SortGames() {
        List<String> gamesHighestValues = new ArrayList<>();
        List<String> normalGames = new ArrayList<>();
        int highest_value;
        int roundCounter=1;
        int loops = Math.ceilDiv(maxMinigameRounds, gamesToBePlayed.size());
        int totalMinigames;
        boolean addbreak=false;



        for(int l = 1; l <= loops  ;  l++) {

            //get total number of games excluding breaks
            totalMinigames=0;
            for(Map.Entry<String, Integer> game  : this.gamesToBePlayed.entrySet()) {
                if(!game.getKey().equalsIgnoreCase("break")){
                    totalMinigames+=1;
                }
            }

            //accommodate for the first game played cannot be selected and therefore taken out of the equasion
            if(l==1){
                totalMinigames-=1;
            }

            int sets = Math.floorDiv(totalMinigames, 3);

            //create a list of the highest value games
            //in a normal match of 22 there are 7 high value games found
            gamesHighestValues = new ArrayList<>();
            normalGames = new ArrayList<>();
            highest_value = 10;
            int counter = 0;
            while (counter < sets) {
                for (Map.Entry<String, Integer> game : this.gamesToBePlayed.entrySet()) {
                    if(l!=1 || !game.getKey().equalsIgnoreCase(this.fisrtGame)) {
                        if (game.getValue() == highest_value && !game.getKey().equalsIgnoreCase("break")) {
                            gamesHighestValues.add(game.getKey());
                            counter += 1;
                            if (counter == sets) {
                                break;
                            }
                        }
                    }
                }
                highest_value -= 1;
            }

            //create a list of all other games
            for (Map.Entry<String, Integer> game : this.gamesToBePlayed.entrySet()) {
                if(l!=1 || !game.getKey().equalsIgnoreCase(this.fisrtGame)) {
                    if (!gamesHighestValues.contains(game.getKey()) && !game.getKey().equalsIgnoreCase("break")) {
                        normalGames.add(game.getKey());
                    }
                }
            }


            if (mainConfig.getBoolean("halfTime-break")) {
                addbreak = true;
            }

            int middle = Math.floorDiv(maxMinigameRounds-1, 2);
            //counter being used to keep track of where high value games should be placed
            counter = 1;
            for (int i = 1; i <= totalMinigames; i++) {

                if (addbreak && roundCounter == middle) {
                    addbreak = false;
                    gamelist.add("break");
                    i--;
                } else if ((3 * counter) - 1 == i && counter <= sets) {
                    counter += 1;
                    roundCounter += 1;
                    String randomGame = gamesHighestValues.get(new Random().nextInt(gamesHighestValues.size()));
                    gamesHighestValues.remove(randomGame);
                    gamelist.add(randomGame);
                } else {
                    roundCounter += 1;
                    String randomGame = normalGames.get(new Random().nextInt(normalGames.size()));
                    normalGames.remove(randomGame);
                    gamelist.add(randomGame);
                }
            }
        }
        Bukkit.getLogger().warning("Game order = " + gamelist);
        PrepareNextGame();
    }

    // Always prepares a game in advance
    @EventHandler
    public void onMinigameStart(PGStartMiniGameEvent event) {
        playedRounds+=1;
        PrepareNextGame();
    }

    private void PrepareNextGame() {
        if (gamesToBePlayed.isEmpty() || playedRounds>=maxMinigameRounds) {
            arena.minigamesToPlay.clear();
            MMPartyGamesExtensions.getInstance().arenaHandlers.remove(this);
        }

        var game = gamelist.getFirst();
        Bukkit.getLogger().warning("Selected game = " + game);
        gamelist.removeFirst();
        arena.minigamesToPlay = new ArrayList<>(List.of(game));
        Bukkit.getLogger().warning("New internal PG list = " + arena.minigamesToPlay.toString());
    }
}
