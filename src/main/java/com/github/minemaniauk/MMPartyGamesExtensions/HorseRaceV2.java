package com.github.minemaniauk.MMPartyGamesExtensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import me.wazup.partygames.PartyGames;
import me.wazup.partygames.PartyGamesAPI;
import me.wazup.partygames.managers.SoundsManager;
import me.wazup.partygames.utils.Cuboid;
import me.wazup.partygames.utils.Utils;
import me.wazup.partygames.utils.XMaterial;
import me.wazup.partygames.utils.Enums.PlayCounter;
import me.wazup.partygames.minigames.MiniGame;
import me.wazup.partygames.minigames.MiniGameMap;
import me.wazup.partygames.utils.Cuboid;
import me.wazup.partygames.PartyGamesAPI;
import me.wazup.partygames.PartyGames;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.attribute.Attribute;




public class HorseRaceV2 extends MiniGame {
    int roundDuration;
    List<ItemStack> playerItems;
    PartyGames plugin;

    public HorseRaceV2(PartyGames var1) {
        this.plugin = var1;
    }

    public String getActualName() {
        return "Horse RaceV2";
    }

    public Material getRepresentingMaterial() {
        return XMaterial.DIAMOND_HORSE_ARMOR.parseMaterial();
    }

    public void handleSetupCommand(Player var1, String[] var2) {
        if (var2.length == 0) {
            var1.sendMessage(ChatColor.DARK_GREEN + " -------- " + ChatColor.GREEN + this.getActualName() + " Commands " + ChatColor.DARK_GREEN + " -------- ");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "CreateMap <Name>" + ChatColor.RED + " - Requires selection");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "DeleteMap <Name>");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "AddSpawn <Map>");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "RemoveSpawn <Map>");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "SetWinningRegion <Map>" + ChatColor.RED + " - Requires selection");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "SetLosingRegion <Map>" + ChatColor.RED + " - Requires selection");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "SetBarrier <Map>" + ChatColor.RED + " - Requires selection");
            var1.sendMessage(ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + "List");
        } else {
            String var3 = var2[0].toLowerCase();
            if (var3.equals("createmap")) {
                this.createMapCommand(var1, var2, true, false);
            } else if (var3.equals("deletemap")) {
                this.deleteMapCommand(var1, var2);
            } else if (var3.equals("addspawn")) {
                if (var2.length == 1) {
                    var1.sendMessage(this.getPrefix() + "You must specify a map name!");
                } else {
                    String var11 = var2[1];
                    if (!this.maps.containsKey(var11.toLowerCase())) {
                        var1.sendMessage(this.getPrefix() + "Could not find a map with that name!");
                    } else {
                        HorseRaceV2Map var14 = (HorseRaceV2Map)this.maps.get(var11.toLowerCase());
                        var14.spawnpoints.add(var1.getLocation());
                        int var17 = var14.spawnpoints.size();
                        FileConfiguration var19 = this.getConfiguration();
                        var19.set("Maps." + var11 + ".Spawnpoints." + var17, Utils.getStringFromLocation(var1.getLocation(), true));
                        this.saveConfiguration(var19);
                        var1.sendMessage(this.getPrefix() + "Map spawnpoint with id #" + var17 + " has been set successfully!");
                    }
                }
            } else if (var3.equals("removespawn")) {
                if (var2.length == 1) {
                    var1.sendMessage(this.getPrefix() + "You must specify a map name!");
                } else {
                    String var10 = var2[1];
                    if (!this.maps.containsKey(var10.toLowerCase())) {
                        var1.sendMessage(this.getPrefix() + "Could not find a map with that name!");
                    } else {
                        HorseRaceV2Map var13 = (HorseRaceV2Map)this.maps.get(var10.toLowerCase());
                        if (var13.spawnpoints.isEmpty()) {
                            var1.sendMessage(this.getPrefix() + "That map does not have any spawnpoints!");
                        } else {
                            int var16 = var13.spawnpoints.size();
                            Location var18 = (Location)var13.spawnpoints.get(var16 - 1);
                            var13.spawnpoints.remove(var18);
                            FileConfiguration var8 = this.getConfiguration();
                            var8.set("Maps." + var10 + ".Spawnpoints." + var16, (Object)null);
                            this.saveConfiguration(var8);
                            var1.sendMessage(this.getPrefix() + "Map spawnpoint with id #" + var16 + " has been removed successfully!");
                        }
                    }
                }
            } else if (!var3.equals("setwinningregion") && !var3.equals("setlosingregion")) {
                if (var3.equals("setbarrier")) {
                    if (var2.length == 1) {
                        var1.sendMessage(this.getPrefix() + "You must specify a map name!");
                    } else {
                        String var9 = var2[1];
                        if (!this.maps.containsKey(var9.toLowerCase())) {
                            var1.sendMessage(this.getPrefix() + "Could not find a map with that name!");
                        } else {
                            Cuboid var12 = PartyGamesAPI.getPlayerSelection(var1);
                            if (var12 == null) {
                                var1.sendMessage(this.getPrefix() + "You must select the 2 corners of the barrier that will be removed when the game stars! Use '/pg wand' to receive the selection wand!");
                            } else {
                                PartyGamesAPI.removePlayerSelection(var1);
                                ((HorseRaceV2Map)this.maps.get(var9.toLowerCase())).barrierCuboid = var12;
                                FileConfiguration var15 = this.getConfiguration();
                                var15.set("Maps." + var9 + ".Barrier-Cuboid", var12.toString());
                                this.saveConfiguration(var15);
                                var1.sendMessage(this.getPrefix() + "Barrier has been set successfully!");
                            }
                        }
                    }
                } else if (var3.equals("list")) {
                    this.listCommand(var1);
                } else {
                    var1.sendMessage(this.getPrefix() + "Unknown command!");
                }
            } else if (var2.length == 1) {
                var1.sendMessage(this.getPrefix() + "You must specify a map name!");
            } else {
                String var4 = var2[1];
                if (!this.maps.containsKey(var4.toLowerCase())) {
                    var1.sendMessage(this.getPrefix() + "Could not find a map with that name!");
                } else {
                    Cuboid var5 = PartyGamesAPI.getPlayerSelection(var1);
                    boolean var6 = var3.equals("setwinningregion");
                    if (var5 == null) {
                        var1.sendMessage(this.getPrefix() + "You must select the 2 corners of the " + (var6 ? "winning" : "losing") + " region! Use '/pg wand' to receive the selection wand!");
                    } else {
                        PartyGamesAPI.removePlayerSelection(var1);
                        if (var6) {
                            ((HorseRaceV2Map)this.maps.get(var4.toLowerCase())).winningCuboid = var5;
                        } else {
                            ((HorseRaceV2Map)this.maps.get(var4.toLowerCase())).losingCuboid = var5;
                        }

                        FileConfiguration var7 = this.getConfiguration();
                        var7.set("Maps." + var4 + (var6 ? ".Winning-Cuboid" : ".Losing-Cuboid"), var5.toString());
                        this.saveConfiguration(var7);
                        var1.sendMessage(this.getPrefix() + (var6 ? "Winning" : "Losing") + " region has been set successfully!");
                    }
                }
            }
        }
    }

    public void load() {
        this.maps = new HashMap();
        FileConfiguration var1 = this.getConfiguration();
        boolean var2 = false;
        if (!var1.contains("Tutorial-Messages")) {
            var1.set("Tutorial-Messages", Arrays.asList("&2 --------- &aHorse Race &2--------- ", "- Get to the end of the racing course as fast as possible!"));
            var2 = true;
        }

        if (!var1.contains("Display-Name")) {
            var1.set("Display-Name", this.getActualName());
            var2 = true;
        }

        if (!var1.contains("Coins-Per-Minigame-Win")) {
            var1.set("Coins-Per-Minigame-Win", 10);
            var2 = true;
        }

        if (!var1.contains("Round-Duration")) {
            var1.set("Round-Duration", 50);
            var2 = true;
        }

        if (!var1.contains("Player-Items")) {
            var1.set("Player-Items", Arrays.asList("BOW : 1", "ARROW : 64"));
            var2 = true;
        }

        if (var2) {
            this.saveConfiguration(var1);
        }

        List var3 = var1.getStringList("Tutorial-Messages");
        this.tutorialMessages = new String[var3.size()];

        for(int var4 = 0; var4 < this.tutorialMessages.length; ++var4) {
            this.tutorialMessages[var4] = Utils.colorize((String)var3.get(var4));
        }

        this.displayName = Utils.colorize(var1.getString("Display-Name"));
        this.coinsPerWin = var1.getInt("Coins-Per-Minigame-Win");
        if (var1.getConfigurationSection("Maps") != null && !var1.getConfigurationSection("Maps").getKeys(false).isEmpty()) {
            for(String var5 : var1.getConfigurationSection("Maps").getKeys(false)) {
                Cuboid var6 = new Cuboid(var1.getString("Maps." + var5 + ".Map-Cuboid"));
                Cuboid var7 = var1.contains("Maps." + var5 + ".Winning-Cuboid") ? new Cuboid(var1.getString("Maps." + var5 + ".Winning-Cuboid")) : null;
                Cuboid var8 = var1.contains("Maps." + var5 + ".Losing-Cuboid") ? new Cuboid(var1.getString("Maps." + var5 + ".Losing-Cuboid")) : null;
                Cuboid var9 = var1.contains("Maps." + var5 + ".Barrier-Cuboid") ? new Cuboid(var1.getString("Maps." + var5 + ".Barrier-Cuboid")) : null;
                ArrayList var10 = new ArrayList();
                if (var1.getConfigurationSection("Maps." + var5 + ".Spawnpoints") != null && !var1.getConfigurationSection("Maps." + var5 + ".Spawnpoints").getKeys(false).isEmpty()) {
                    for(String var12 : var1.getConfigurationSection("Maps." + var5 + ".Spawnpoints").getKeys(false)) {
                        var10.add(Utils.getLocationFromString(var1.getString("Maps." + var5 + ".Spawnpoints." + var12)));
                    }
                }

                HorseRaceV2Map var17 = new HorseRaceV2Map(var5, var6, var7, var8, var9, var10);
                this.maps.put(var5.toLowerCase(), var17);
            }
        }

        this.roundDuration = var1.getInt("Round-Duration");
        this.playerItems = new ArrayList();

        for(String var16 : var1.getStringList("Player-Items")) {
            try {
                this.playerItems.add(PartyGamesAPI.getItemStackFromString(var16));
            } catch (Exception var13) {
                Bukkit.getConsoleSender().sendMessage(this.getPrefix() + ChatColor.RED + "Failed to load an item in horse raceV2. The item has the following text: " + var16);
            }
        }

    }

    public int getAliveCount(String var1) {
        HorseRaceV2Map var2 = (HorseRaceV2Map)this.maps.get(var1.toLowerCase());
        return var2.alive.size();
    }

    private class HorseRaceV2Map extends MiniGameMap {
        Cuboid mainCuboid;
        Cuboid winningCuboid;
        Cuboid losingCuboid;
        Cuboid barrierCuboid;
        List<Location> spawnpoints;
        ArrayList<String> alive;
        ArrayList<String> finished;
        String[] topPlayers;
        boolean allowHorseDamage;
        ArrayList<BlockState> rollback;
        BukkitTask task;

        public HorseRaceV2Map(String var2, Cuboid var3, Cuboid var4, Cuboid var5, Cuboid var6, List<Location> var7) {
            this.name = var2;
            this.mainCuboid = var3;
            this.winningCuboid = var4;
            this.losingCuboid = var5;
            this.barrierCuboid = var6;
            this.spawnpoints = var7;
            this.alive = new ArrayList();
            this.finished = new ArrayList();
            this.topPlayers = new String[3];
            this.allowHorseDamage = false;
            this.rollback = new ArrayList();
        }

        public boolean isSetup() {
            return this.winningCuboid != null && this.losingCuboid != null && this.barrierCuboid != null && !this.spawnpoints.isEmpty();
        }

        public void start() {
            for(int var1 = 0; var1 < this.topPlayers.length; ++var1) {
                this.topPlayers[var1] = null;
            }

//            for(Block var2 : this.barrierCuboid) {
//                this.rollback.add(var2.getState());
//            }
            Iterator<Block> it = this.barrierCuboid.iterator();

            while (it.hasNext()) {
                Block block = it.next();
                this.rollback.add(block.getState());
            }

            int var6 = 0;

            for(Player var4 : this.arena.getPlayers()) {
                if (var6 == this.spawnpoints.size()) {
                    var6 = 0;
                }

                var4.teleport((Location)this.spawnpoints.get(var6++));
                this.alive.add(var4.getName());
            }

            for(Player var8 : this.arena.getSpectators()) {
                var8.teleport((Location)this.spawnpoints.get(0));
            }

            this.startTask();
        }

        public void cancel(boolean var1) {
            this.arena = null;
            this.alive.clear();
            this.finished.clear();
            this.mainCuboid.clearEntities();

            for(BlockState var3 : this.rollback) {
                var3.update(true);
            }

            this.rollback.clear();
            this.allowHorseDamage = false;
            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }

        }

        public void leave(Player var1) {
            this.finished.remove(var1.getName());
            if (this.alive.contains(var1.getName())) {
                this.alive.remove(var1.getName());
                if (this.arena.scoreboard != null) {
                    this.arena.scoreboard.updatePlaceholder("%alive_players%", this.alive.size());
                }

                if (this.alive.size() == 1) {
                    this.finish();
                }
            }

        }

        private void startTask() {
            this.task = (new BukkitRunnable() {
                int releaseTimer = 5;
                int timer;

                {
                    this.timer = HorseRaceV2.this.roundDuration;
                }

                public void run() {
                    if (this.releaseTimer > 0) {
                        --this.releaseTimer;
                        if (this.releaseTimer == 4) {
                            HorseRaceV2Map.this.arena.sendPlayCounterNotification(PlayCounter.READY);
                            PotionEffect var1 = new PotionEffect(PotionEffectType.SPEED, 99999, 1);

                            for(Player var3 : HorseRaceV2Map.this.arena.getPlayers()) {
                                Horse var4 = (Horse)var3.getLocation().getWorld().spawnEntity(var3.getLocation(), EntityType.HORSE);
                                var4.setAdult();
                                var4.setCustomName(var3.getName() + "'s Horse");
                                var4.setOwner(var3);
                                var4.setMaxHealth((double)40.0F);
                                var4.setHealth((double)40.0F);
                                var4.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                                var4.addPotionEffect(var1);
                                var4.setPassenger(var3);
                                var4.getAttribute(Attribute.MOVEMENT_SPEED)
                                        .setBaseValue(0.35);
                            }
                        } else if (this.releaseTimer == 2) {
                            HorseRaceV2Map.this.arena.sendPlayCounterNotification(PlayCounter.SET);
                        } else if (this.releaseTimer == 0) {
                            HorseRaceV2Map.this.arena.sendPlayCounterNotification(PlayCounter.GO);

                            for(Player var8 : HorseRaceV2Map.this.arena.getPlayers()) {
                                for(ItemStack var12 : HorseRaceV2.this.playerItems) {
                                    var8.getInventory().addItem(new ItemStack[]{var12});
                                }
                            }

                            HorseRaceV2Map.this.allowHorseDamage = true;
                            Iterator var6 = HorseRaceV2Map.this.barrierCuboid.iterator();

                            while(var6.hasNext()) {
                                ((Block)var6.next()).setType(Material.AIR);
                            }
                        }
                    } else {
                        for(Player var9 : HorseRaceV2Map.this.arena.getPlayers()) {
                            if (HorseRaceV2Map.this.winningCuboid.contains(var9.getLocation())) {
                                if (!HorseRaceV2Map.this.finished.contains(var9.getName())) {
                                    if (HorseRaceV2Map.this.finished.size() < HorseRaceV2Map.this.topPlayers.length) {
                                        HorseRaceV2Map.this.topPlayers[HorseRaceV2Map.this.finished.size()] = var9.getName();
                                    }

                                    HorseRaceV2Map.this.finished.add(var9.getName());
                                    var9.sendMessage(PartyGamesAPI.getMessage("Player-Finish-Round"));
                                    var9.playSound(var9.getLocation(), SoundsManager.FIREWORK_BLAST, 1.0F, 1.0F);
                                    if (HorseRaceV2Map.this.finished.size() == HorseRaceV2Map.this.alive.size()) {
                                        HorseRaceV2Map.this.finish();
                                    }
                                }
                            } else if (HorseRaceV2Map.this.losingCuboid.contains(var9.getLocation()) || var9.getLocation().getY() < (double) HorseRaceV2Map.this.losingCuboid.y1) {
                                Entity var11 = var9.getVehicle();
                                var9.teleport((Location) HorseRaceV2Map.this.spawnpoints.get(0));
                                if (var11 != null) {
                                    var11.teleport((Location) HorseRaceV2Map.this.spawnpoints.get(0));
                                    var11.setPassenger(var9);
                                }
                            }
                        }

                        --this.timer;
                        if (this.timer == 0) {
                            HorseRaceV2Map.this.finish();
                        }
                    }

                }
            }).runTaskTimer(HorseRaceV2.this.plugin, 40L, 20L);
        }

        private void finish() {
            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }

            PartyGamesAPI.sendRoundConclusion(this.arena, this.topPlayers);
            if (this.finished.isEmpty()) {
                this.arena.finishCurrentMinigame(this.topPlayers);
            } else {
                String var1 = (String)this.finished.get(0);
                final Player var2 = Bukkit.getPlayer(var1);
                this.task = (new BukkitRunnable() {
                    int timer = 5;
                    boolean fireworks = false;

                    public void run() {
                        --this.timer;
                        this.fireworks = !this.fireworks;
                        if (this.fireworks && var2 != null) {
                            Utils.fireWorkEffect(var2, true);
                        }

                        if (this.timer == 0) {
                            HorseRaceV2Map.this.arena.finishCurrentMinigame(HorseRaceV2Map.this.topPlayers);
                        }

                    }
                }).runTaskTimer(HorseRaceV2.this.plugin, 0L, 20L);
            }
        }

        public void handlePlayerInteractEvent(PlayerInteractEvent var1) {
            Player var2 = var1.getPlayer();
            if (this.alive.contains(var2.getName())) {
                var1.setCancelled(false);
            }

        }

        @EventHandler
        public void onHorseDamage(EntityDamageEvent event) {
            if (event.getEntity() instanceof Horse) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);
                }
            }
        }

        public void handleEntityDamageByEntityEvent(EntityDamageByEntityEvent var1) {
            if (var1.getEntityType().equals(EntityType.HORSE) && var1.getDamager().getType().equals(EntityType.PLAYER)) {
                Player var2 = (Player)var1.getDamager();
                if (this.alive.contains(var2.getName()) && !this.allowHorseDamage) {
                    var1.setCancelled(true);
                }
            }

        }
    }
}

