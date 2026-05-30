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
        //get the weight of the first game (hopefully)
        int intensityScore = weightsConfig.getInteger("game-weights." + this.arena.currentMinigame.getActualName().toLowerCase().replace(" ", ""));
        LinkedHashMap<String, Integer> gamesToBePlayedTemp = new LinkedHashMap<>();

        while(arena.minigamesToPlay.size() != gamesToBePlayedTemp.size()){
            Map.Entry<String, Integer> game = null;

            //get the opposite side of the range
            int score = Math.abs(intensityScore - 10);

            //these if statements make push it more to the extreme values otherwise intensity 1 and 10 games will always be last
            if(score>5 & score!=9){
                score+=2;
            }
            else if (score<5 & score!=1){
                score-=2;
            }

            List<String> appropriateGames = new ArrayList<>();
            int counter=1;

            while(appropriateGames.isEmpty()){
                appropriateGames =  new ArrayList<>();
                for(Map.Entry<String, Integer> game1  : this.gamesToBePlayed.entrySet()) {
                    // group up games of appropriate intensity values and pick them
                    //if no games are selected counter increases increasing the range for allowed games
                    if(game1.getValue() >= score-counter & game1.getValue() <= score + counter){
                        appropriateGames.add(game1.getKey());
                    }
                }
                counter+=1;

            }

            intensityScore = Math.floorDiv((intensityScore + game.getValue()), 2);

            String randomGame = appropriateGames.get(new Random().nextInt(appropriateGames.size()));

            gamesToBePlayedTemp.put(randomGame, gamesToBePlayedTemp.get(randomGame));
            gamesToBePlayed.remove(randomGame);
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
