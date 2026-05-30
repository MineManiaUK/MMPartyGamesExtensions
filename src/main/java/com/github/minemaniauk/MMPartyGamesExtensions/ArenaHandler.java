package com.github.minemaniauk.MMPartyGamesExtensions;

import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import me.wazup.partygames.Arena;
import me.wazup.partygames.PartyGames;
import me.wazup.partygames.events.PGStartMiniGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class ArenaHandler implements Listener {

    public final String arenaName;
    public final Arena arena;
    public Configuration weightsConfig;

    private LinkedHashMap<String, Integer> gamesToBePlayed = new LinkedHashMap<>();

    public ArenaHandler(String arenaName) {
        this.arenaName = arenaName;
        this.arena = PartyGames.getInstance().arenas.get(arenaName);
        this.weightsConfig = MMPartyGamesExtensions.getInstance().getGameWeightsConfig();

        if (this.arena == null) {
            Bukkit.getLogger().warning("Tried the create arena Arena Handler but could not find arena in PG plugin");
            MMPartyGamesExtensions.getInstance().RemoveArenaHandler(arenaName);
            return;
        }

        for (String game : arena.minigamesToPlay) {
            gamesToBePlayed.put(game, weightsConfig.getInteger("game-weights." + game));
        }

        SortGames();
    }

    public void SortGames() {
        //TODO spread high stress games from low stress games
        //get the weight of the fisrt game (hopefully)
        Integer intenstyScore = weightsConfig.getInteger("game-weights." + this.arena.currentMinigame.getActualName().toLowerCase().replace(" ", ""));
        LinkedHashMap<String, Integer> gamesToBePlayedTemp = new LinkedHashMap<>();



        while(arena.minigamesToPlay.size() != gamesToBePlayedTemp.size()){
            Map.Entry<String, Integer> game = null;
            
            
            
            //get the opersit side of the range
            int score = Math.abs(intenstyScore-10);

            //these if stamets make push it more to the extrame values otherwise intensity 1 and 10 games will always be last
            if(score>5 & score!=9){
                score+=2;
            }
            else if (score<5 & score!=1){
                score-=2;
            }

            List<Object> appropraiteGames = new ArrayList<>();
            int counter=1;

            while(appropraiteGames.size()==0){
                appropraiteGames =  new ArrayList<>();
                for(Map.Entry<String, Integer> game1  : this.gamesToBePlayed.entrySet()) {
                    // group up games of appropte intesity values and pick them
                    //if no games are seclected counter increses incresing the range for allowed games
                    if(game1.getValue() >= score-counter & game1.getValue() <= score + counter){
                        appropraiteGames.add(game1);
                    }
                }
                counter+=1;
                
                object[] test = appropraiteGames;

                MiniGame random_game = (MiniGame)test[Utils.random.nextInt(test.length)];

            }
            intenstyScore = Math.floorDiv((intenstyScore+game.getValue()),2);

            gamesToBePlayedTemp.add(random_game);
            gamesToBePlayed.remove(random_game);
        }

        gamesToBePlayed = gamesToBePlayedTemp;

        Bukkit.getLogger().warning("Game order = " + gamesToBePlayed.toString());

        PrepareNextGame();
    }

    // Always prepares a game in advance
    @EventHandler
    public void onMinigameStart(PGStartMiniGameEvent event) {
        PrepareNextGame();
    }

    private void PrepareNextGame() {
        if (gamesToBePlayed.isEmpty()) {
            arena.minigamesToPlay.clear();
            MMPartyGamesExtensions.getInstance().arenaHandlers.remove(this);
        }

        var game = gamesToBePlayed.firstEntry().getKey();
        Bukkit.getLogger().warning("Selected game = " + game);
        gamesToBePlayed.remove(game);
        arena.minigamesToPlay = new ArrayList<>(List.of(game));
        Bukkit.getLogger().warning("New internal PG list = " + arena.minigamesToPlay.toString());
    }
}
