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
