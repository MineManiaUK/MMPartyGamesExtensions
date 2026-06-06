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
        LinkedHashMap<String, Integer> gamesToBePlayedTemp = new LinkedHashMap<>();
        List<String> gamesHighestValues = new LinkedHashMap<>();
        List<String> normalGames = new LinkedHashMap<>();

        Integer sets = Math.floorDiv(gamesToBePlayed.size(), 3);
        Intiger highest_value = 10;
        Intiger counter = 0;

        //create a list of the highest value games 
        //in a normal match of 22 there are 7 high value games found 
        while(counter < sets){
            for(Map.Entry<String, Integer> game  : this.gamesToBePlayed.entrySet()) {             
                    if(game.getValue() == highest_value){
                        gamesHighestValues.add(game.getKey());
                        counter+=1;
                        if(counter==sets){
                            break;
                        }
                    }
            }
            highest_value-=1;
        }

        //create a list of all other games
        for(Map.Entry<String, Integer> game  : this.gamesToBePlayed.entrySet()) {
            if(!gamesHighestValues.contains(game.getKey())){
                normalGames.add(game.getKey());
            }
        }

        //counter being used to keep track of where high value games should be placed
        counter=1;
        for (int i = 1; i <= gamesToBePlayed.size()  ; i = i ++) {
            if((3*counter)-1 == i && counter<=sets){
                counter+=1;
                String randomGame = gamesHighestValues.get(new Random().nextInt(gamesHighestValues.size()));
                gamesHighestValues.remove(randomGame);
                gamesToBePlayedTemp.put(randomGame, gamesToBePlayed.get(randomGame));
            }
            else{
                String randomGame = normalGames.get(new Random().nextInt(normalGames.size()));
                normalGames.remove(randomGame);
                gamesToBePlayedTemp.put(randomGame, gamesToBePlayed.get(randomGame));
            }
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
