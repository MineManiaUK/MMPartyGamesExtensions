package com.github.minemaniauk.MMPartyGamesExtensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import me.wazup.partygames.minigames.HorseRace;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import me.wazup.partygames.utils.XMaterial;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import me.wazup.partygames.utils.Utils;

import me.wazup.partygames.utils.Enums.PlayCounter;
import me.wazup.partygames.utils.Cuboid;
import me.wazup.partygames.PartyGamesAPI;
import me.wazup.partygames.PartyGames;
import me.wazup.partygames.minigames.MiniGame;
import me.wazup.partygames.minigames.MiniGameMap;

public class Break extends MiniGame {

    String prefix = ChatColor.DARK_GREEN + "[Break] " + ChatColor.GREEN;

    String[] tutorialMessages;
    int roundDuration;
    PartyGames plugin;
    List<ItemStack> playerItems;

    public Break(PartyGames var1) {
        this.plugin = var1;

    }


    // public void onEnable() {
    //     plugin.registerMinigame(new Break(this.plugin));
    // }

    @Override
    public Material getRepresentingMaterial() {
        return Material.CLOCK;
    }


   @Override
    public String getActualName() {
        return "Break";
    }


    public String getName() {
        return "Break";
    }


    @Override
    public String[] getTutorialMessages() {
        return tutorialMessages;
    }

    @Override
    public void handleSetupCommand(Player var1, String[] var2) {
        if (var2.length == 0) {
            var1.sendMessage(ChatColor.DARK_GREEN + " -------- " + ChatColor.GREEN + this.getActualName() + " Commands " + ChatColor.DARK_GREEN + " -------- ");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "CreateMap <Name>" + ChatColor.RED + " - Requires selection");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "DeleteMap <Name>");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "SetSpawn <Map>");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "List");
        } else {
            String var3 = var2[0].toLowerCase();
            if (var3.equals("createmap")) {
                this.createMapCommand(var1, var2, true, false);
            } else if (var3.equals("deletemap")) {
                this.deleteMapCommand(var1, var2);
            } else if (var3.equals("setspawn")) {
                this.setSpawnCommand(var1, var2);
            } else if (var3.equals("list")) {
                this.listCommand(var1);
            } else {
                var1.sendMessage(this.getPrefix() + "Unknown command!");
            }
        }
    }

    @Override
    public void load() {
        maps = new HashMap<String, MiniGameMap>();

        FileConfiguration config = getConfiguration();

        boolean save = false;
        if(!config.contains("Tutorial-Messages")) {
            config.set("Tutorial-Messages", Arrays.asList(new String[] {
                    "&2 --------- &aBreak &2--------- ",
                    "- this is half time break",
                    ""
            }));
            save = true;
        }

        if (!config.contains("Display-Name")) {
            config.set("Display-Name", this.getActualName());
            save = true;
        }

        if (!config.contains("Coins-Per-Minigame-Win")) {
            config.set("Coins-Per-Minigame-Win", 0);
            save = true;
        }

        // 4 minuets
        if(!config.contains("Round-Duration")) {
            config.set("Round-Duration", 240);
            save = true;
        }
        if(save) saveConfiguration(config);

        this.displayName = Utils.colorize(config.getString("Display-Name"));
        this.coinsPerWin = config.getInt("Coins-Per-Minigame-Win");


        List<String> temp = config.getStringList("Tutorial-Messages");
        tutorialMessages = new String[temp.size()];
        for(int i = 0; i < tutorialMessages.length; i++) tutorialMessages[i] = ChatColor.translateAlternateColorCodes('&', temp.get(i));

        if(config.getConfigurationSection("Maps") != null && !config.getConfigurationSection("Maps").getKeys(false).isEmpty()) {
            for(String mapName: config.getConfigurationSection("Maps").getKeys(false)) {
                Cuboid cuboid = new Cuboid(config.getString("Maps." + mapName + ".Map-Cuboid"));
                Location spawnpoint = config.contains("Maps." + mapName + ".Spawnpoint") ? Utils.getLocationFromString(config.getString("Maps." + mapName + ".Spawnpoint")) : null;
                maps.put(mapName.toLowerCase(), new BreakMap(mapName, cuboid, spawnpoint));
            }
        }
        roundDuration = config.getInt("Round-Duration");
    }

    @Override
    public int getAliveCount(String mapName) {
        BreakMap map = (BreakMap) maps.get(mapName.toLowerCase());
        return map.alive.size();
    }

    private class BreakMap extends MiniGameMap implements Listener {
        Cuboid mainCuboid;
        Location spawnpoint;
        ArrayList<String> alive;
        String[] topPlayers;
        HashMap<Location, BlockState> rollback;
        BukkitTask task;
        boolean allowBreaking;

        public BreakMap(String name, Cuboid mainCuboid, Location spawnpoint) {
            this.name = name;
            this.mainCuboid = mainCuboid;
            this.spawnpoint = spawnpoint;
            alive = new ArrayList<String>();
            topPlayers = new String[3];
            rollback = new HashMap<Location, BlockState>();
            allowBreaking = false;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
        public boolean isSetup() {
            return  spawnpoint != null;
        }

        public void start() {
            for(int i = 0; i < topPlayers.length; i++) topPlayers[i] = null;

            for(Player p: arena.getPlayers()) {
                p.teleport(spawnpoint);
                alive.add(p.getName());
            }

            for(Player var8 : arena.getPlayers()) {
                ItemStack rod = new ItemStack(Material.FISHING_ROD);
                rod.addEnchantment(Enchantment.LURE, 3);
                rod.addEnchantment(Enchantment.LUCK_OF_THE_SEA, 3);
                rod.addEnchantment(Enchantment.UNBREAKING, 3);
                rod.getItemMeta().setUnbreakable(true);
                var8.getInventory().addItem(rod);
            }
            for(Player p: arena.getSpectators()) p.teleport(spawnpoint);
            startTask();
        }

        public void cancel(boolean immediate) {
            allowBreaking = false;
            alive.clear();
            arena = null;
            PartyGamesAPI.rollback(rollback, immediate);
            mainCuboid.clearEntities();
            if(task != null) {
                task.cancel();
                task = null;
            }
        }

        public void leave(Player p) {
            if(alive.contains(p.getName())) {
                alive.remove(p.getName());
                if(arena.scoreboard != null) this.arena.scoreboard.updatePlaceholder("%alive_players%", this.alive.size());;
                if(alive.size() == 1) {
                    finish();
                }
            }
        }

        private void startTask() {
            task = new BukkitRunnable() {
                int breakingTimer = 1;
                int timer = roundDuration;
                public void run() {
                    if(breakingTimer > 0) {
                        breakingTimer--;
                    } else {
                        for(Player p: arena.getPlayers()) {
                            p.setLevel(timer);
                            if(alive.contains(p.getName())) {

                            }
                        }
                        timer--;
                        if(timer == 0) {
                            finish();
                        }
                    }
                }
            }.runTaskTimer(plugin, 40L, 20L);
        }

        private void finish() {
            if(task != null) {
                task.cancel();
                task = null;
            }
            PartyGamesAPI.sendRoundConclusion(this.arena, this.topPlayers);
            this.arena.finishCurrentMinigame(this.topPlayers);
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onBlockBreakEvent(BlockBreakEvent e) {
            Player p = e.getPlayer();

        }

        @EventHandler
        public void onProjectileHitEvent(ProjectileHitEvent e) {

        }

        @EventHandler
        public void onPlayerDropItem(PlayerDropItemEvent var1) {
            if (arena.getPlayers().contains(var1.getPlayer())) {
                var1.setCancelled(false);
            }

        }

        @EventHandler
        public void onPlayerPickupItem(PlayerPickupItemEvent var1) {
            if(arena!=null) {
                if (arena.getPlayers().contains(var1.getPlayer())) {
                    var1.setCancelled(false);
                }
            }
        }

        public void handlePlayerInteractEvent(PlayerInteractEvent e) {
            e.setCancelled(false);
        }
        public void handleEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {}

    }

}
