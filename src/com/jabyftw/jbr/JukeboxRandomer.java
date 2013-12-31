package com.jabyftw.jbr;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Rafael
 */
@SuppressWarnings("FieldMayBeFinal")
public class JukeboxRandomer extends JavaPlugin implements Listener {

    private CustomConfig configYML;
    private FileConfiguration config;
    private Material record;
    private Location loc;
    private int quantity, delay;
    private Random r = new Random();
    private RandomCollection<ItemStack> items = new RandomCollection();

    @Override
    public void onEnable() {
        configYML = new CustomConfig(this, "config");
        config = configYML.getCustomConfig();
        config.addDefault("config.jukeboxLocation", "world;5;64;-5");
        config.addDefault("config.jukeboxRecord", "record_3");
        config.addDefault("config.delayInHour", 24);
        config.addDefault("config.itemQuantity", 2);
        String[] is = {"0.05;1;diamond", "0.50;1;apple", "0.45;2;apple"};
        config.addDefault("config.randomItemList", Arrays.asList(is));
        //config.addDefault("lang.", "&");
        config.addDefault("lang.alreadyUsed", "&4You already used it! &cTry later after %hour hour(s).");
        config.addDefault("lang.inventoryTitle", "&6Random Jukebox");
        config.addDefault("cooldown.testing", System.currentTimeMillis());
        config.options().copyDefaults(true);
        configYML.saveCustomConfig();
        saveConfig();
        reloadConfig();
        try {
            String[] s = config.getString("config.jukeboxLocation").split(";");
            loc = new Location(getServer().getWorld(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]));
        } catch (NullPointerException e) {
            getServer().getPluginManager().disablePlugin(this);
        } catch (NumberFormatException e) {
            getServer().getPluginManager().disablePlugin(this);
        }
        record = Material.valueOf(config.getString("config.jukeboxRecord").toUpperCase());
        quantity = config.getInt("config.itemQuantity");
        delay = config.getInt("config.delayInHour");
        for (String s : config.getStringList("config.randomItemList")) {
            String[] s1 = s.split(";");
            items.add(Double.parseDouble(s1[0]), new ItemStack(Material.valueOf(s1[2].toUpperCase()), Integer.parseInt(s1[1])));
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().log(Level.INFO, "Enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Disabled.");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (p.hasPermission("jukebox.use")) {
                if ((e.getClickedBlock().getLocation().distanceSquared(loc) < (4 * 4)) && e.getClickedBlock().getState() instanceof Jukebox) {
                    e.setCancelled(true);
                    if (canUse(p) || p.hasPermission("jukebox.bypass")) {
                        Jukebox j = (Jukebox) e.getClickedBlock().getState();
                        if (!j.isPlaying()) {
                            j.setPlaying(Material.AIR);
                            j.setPlaying(record);
                        }
                        Inventory i = getServer().createInventory(((InventoryHolder) p), 9, getLang("inventoryTitle"));
                        for (int a = 0; a < quantity; a++) {
                            i.setItem(r.nextInt(9), items.next());
                        }
                        p.openInventory(i);
                    } else {
                        p.sendMessage(getLang("alreadyUsed").replaceAll("%hour", Integer.toString(delay)));
                    }
                }
            }
        }
    }

    private String getLang(String path) {
        return config.getString("lang." + path).replaceAll("&", "§");
    }

    private boolean outOfCooldown(long timeUsed) {
        return System.currentTimeMillis() - timeUsed > (delay * 3600 * 1000);
    }

    private boolean canUse(Player p) {
        long last = config.getLong("cooldown." + p.getName().toLowerCase());
        if (last > 0) {
            config.set("cooldown." + p.getName().toLowerCase(), System.currentTimeMillis());
            configYML.saveCustomConfig();
            return outOfCooldown(last);
        } else {
            config.addDefault("cooldown." + p.getName().toLowerCase(), System.currentTimeMillis());
            configYML.saveCustomConfig();
            return true;
        }
    }
}
