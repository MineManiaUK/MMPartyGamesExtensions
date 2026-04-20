package com.github.minemaniauk.MMPartyGamesExtentions;

import org.bukkit.plugin.java.JavaPlugin;
import me.wazup.partygames.*;

public final class MMPartyGamesExtentions extends JavaPlugin {

    private static MMPartyGamesExtentions instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Return the MMPartyGamesExtentions instance
    public static MMPartyGamesExtentions getInstance() {
        return instance;
    }
}
