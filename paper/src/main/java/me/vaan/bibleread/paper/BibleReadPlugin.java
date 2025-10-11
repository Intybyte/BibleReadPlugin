package me.vaan.bibleread.paper;

import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.connection.ConnectionHandler;
import me.vaan.bibleread.api.data.access.PlayerDataManager;
import me.vaan.bibleread.api.file.FileManager;
import me.vaan.bibleread.bukkit.PluginHolder;
import me.vaan.bibleread.bukkit.command.BibleCommandManager;
import me.vaan.bibleread.bukkit.command.MainCommand;
import me.vaan.bibleread.paper.command.PaperCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class BibleReadPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        PluginHolder.initialize(this);
        FileManager.initialize(getDataFolder());
        ConnectionHandler.initialize();
        AccessManager.initialize();
        PlayerDataManager.load();
    }

    @Override
    public void onEnable() {
        BibleCommandManager bcm = new BibleCommandManager(this);
        bcm.registerCommand(new MainCommand());
        bcm.registerCommand(new PaperCommand());
    }

    @Override
    public void onDisable() {
        PlayerDataManager.save();
        ConnectionHandler.shutdown();
    }
}
