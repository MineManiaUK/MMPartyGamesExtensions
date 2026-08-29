package com.github.minemaniauk.MMPartyGamesExtensions;


import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import me.wazup.partygames.events.PGArenaFinishEvent;
import me.wazup.partygames.events.PGArenaStartEvent;
import me.wazup.partygames.events.PGArenaStopEvent;
import me.wazup.partygames.PartyGames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class MMPartyGamesExtensions extends JavaPlugin implements Listener {

    private static MMPartyGamesExtensions instance;
    private Configuration config;
    private Configuration gameWeights;

    public List<ArenaHandler> arenaHandlers = new ArrayList<>();
    PartyGames pg;

    @Override
    public void onEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PartyGames");
        if (plugin == null) {
            getLogger().severe("PartyGames not found!");
        }
        else{
            getLogger().severe("PartyGames is found");
            pg = (PartyGames) plugin; 
            pg.registerMinigame(new Break(pg));
            pg.registerMinigame(new HorseRaceV2(pg));
        }
        
        


        // Plugin startup logic
        instance = this;

        // Set up main config
        this.config = ConfigurationFactory.YAML
                .create(getDataFolder(), "config")
                .setDefaultPath("config.yml");
        this.config.load();

        if (config.get("halfTime-break")==null) {
            config.set("halfTime-break", true);
            config.save();
        }

        // Set up the Game Weight config (Used to Sequence games)
        this.gameWeights = ConfigurationFactory.YAML
                .create(getDataFolder(), "game-weights")
                .setDefaultPath("game-weights.yml");
        this.gameWeights.load();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void arenaStart(PGArenaStartEvent event) {
        var newHandler = new ArenaHandler(event.getName(), pg);
        arenaHandlers.add(newHandler);
        getServer().getPluginManager().registerEvents(newHandler, this);
    }

    @EventHandler
    public void arenaStop(PGArenaStopEvent event) {
        RemoveArenaHandler(event.getName());
    }

    @EventHandler
    public void onArenaFinish(PGArenaFinishEvent event) {
        RemoveArenaHandler(event.getName());
    }

    public void RemoveArenaHandler(String arenaName) {

        arenaName = arenaName.toLowerCase();

        Iterator<ArenaHandler> iterator = arenaHandlers.iterator();

        while (iterator.hasNext()) {
            ArenaHandler arenaHandler = iterator.next();

            if (Objects.equals(arenaHandler.arenaName, arenaName)) {

                HandlerList.unregisterAll(arenaHandler);
                iterator.remove();

                break;
            }
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Returns the Game Weights config
    public Configuration getGameWeightsConfig(){
        return this.gameWeights;
    }

    // Returns the Main Config
    public Configuration getMainConfig(){
        return this.config;
    }

    // Returns the MMPartyGamesExtensions instance
    public static MMPartyGamesExtensions getInstance() {
        return instance;
    }
}
