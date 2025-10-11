package me.vaan.bibleread.bukkit;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginHolder {
    @Getter
    private static JavaPlugin instance = null;

    public static void initialize(JavaPlugin pl) {
        if (instance == null) instance = pl;
    }
}
