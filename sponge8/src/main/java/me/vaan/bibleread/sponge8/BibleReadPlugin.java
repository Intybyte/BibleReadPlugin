package me.vaan.bibleread.sponge8;

import com.google.inject.Inject;
import lombok.Getter;
import me.vaan.bibleread.api.access.AccessManager;
import me.vaan.bibleread.api.connection.ConnectionHandler;
import me.vaan.bibleread.api.data.access.PlayerDataManager;
import me.vaan.bibleread.api.file.FileManager;
import me.vaan.bibleread.sponge8.command.BibleCommandManager;
import me.vaan.bibleread.sponge8.command.MainCommand;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;
import java.util.concurrent.Executor;

/**
 * The main class of your Sponge plugin.
 *
 * <p>All methods are optional -- some common event registrations are included as a jumping-off point.</p>
 */
@Plugin("BibleReadPlugin")
@Getter
public class BibleReadPlugin {

    @Getter
    private static BibleReadPlugin instance;
    private final PluginContainer container;
    private final Logger logger;


    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    private Executor mainExecutor;

    @Inject
    BibleReadPlugin(final PluginContainer container, final Logger logger) {
        this.container = container;
        this.logger = logger;
        instance = this;
    }

    @Listener
    public void onGameLoadEvent(final LoadedGameEvent event) {
        FileManager.initialize(privateConfigDir.toFile());
        ConnectionHandler.initialize();
        AccessManager.initialize();
        PlayerDataManager.load();
        // Perform any one-time setup
        mainExecutor = Sponge.server().scheduler().executor(container);
        BibleCommandManager scm = new BibleCommandManager(container);
        scm.registerCommand(new MainCommand());
    }
}
