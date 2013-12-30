package com.jabyftw.jbr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Rafael
 */
public class CustomConfig {

    private final JukeboxRandomer pl;
    private final String name;
    private File file;
    private FileConfiguration fileConfig;

    public CustomConfig(JukeboxRandomer pl, String name) {
        this.pl = pl;
        this.name = name;
    }

    public FileConfiguration getCustomConfig() {
        if (fileConfig == null) {
            reloadCustomConfig();
        }
        return fileConfig;
    }

    public void reloadCustomConfig() {
        if (fileConfig == null) {
            file = new File(pl.getDataFolder(), name + ".yml");
        }
        fileConfig = YamlConfiguration.loadConfiguration(file);
        InputStream defConfigStream = pl.getResource(name + ".yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfig.setDefaults(defConfig);
        }
    }

    public void saveCustomConfig() {
        if (file == null) {
            file = new File(pl.getDataFolder(), name + ".yml");
        }
        try {
            getCustomConfig().options().copyDefaults(true);
            getCustomConfig().save(file);
        } catch (IOException ex) {
            pl.getLogger().log(Level.WARNING, "Couldn't save .yml");
        }
    }
}
