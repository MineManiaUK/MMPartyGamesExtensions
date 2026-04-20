package com.github.minemaniauk.MMPartyGamesExtentions;

import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import me.wazup.partygames.*;

public final class MMPartyGamesExtentions extends JavaPlugin {

    private static MMPartyGamesExtentions instance;
    private Configuration config;
    private Configuration gameWeights;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // Setup main config
        this.config = ConfigurationFactory.YAML
                .create(getDataFolder(), "config")
                .setDefaultPath("config.yml");
        this.config.load();

        // Setup the Game Weight config (Used to Sequence games)
        this.gameWeights = ConfigurationFactory.YAML
                .create(getDataFolder(), "game-weights")
                .setDefaultPath("game-weights.yml");
        this.gameWeights.load();

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

    // Returns the MMPartyGamesExtentions instance
    public static MMPartyGamesExtentions getInstance() {
        return instance;
    }
}
