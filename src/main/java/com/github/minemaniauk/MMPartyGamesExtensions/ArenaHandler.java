package com.github.minemaniauk.MMPartyGamesExtensions;

import me.wazup.partygames.Arena;
import me.wazup.partygames.PartyGames;
import me.wazup.partygames.events.PGStartMiniGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.stream.Collectors;

public class ArenaHandler implements Listener {

    public final String arenaName;
    public final Arena arena;

    private LinkedHashMap<String, Integer> gamesToBePlayed = new LinkedHashMap<>();

    public ArenaHandler(String arenaName) {
        this.arenaName = arenaName;
        this.arena = PartyGames.getInstance().arenas.get(arenaName);

        if (this.arena == null) {
            Bukkit.getLogger().warning("Tried the create arena Arena Handler but could not find arena in PG plugin");
            MMPartyGamesExtensions.getInstance().arenaHandlers.remove(this);
            return;
        }

        for (String game : arena.minigamesToPlay) {
            gamesToBePlayed.put(game, MMPartyGamesExtensions.getInstance().getGameWeightsConfig().getInteger(game));
        }

        SortGames();
    }

    public void SortGames() {
        // Sorts from lowest to highest right now but will sort games to spread high stress games from low stress games
        gamesToBePlayed = gamesToBePlayed.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
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
        gamesToBePlayed.remove(game);
        arena.minigamesToPlay = List.of(game);
    }

}
