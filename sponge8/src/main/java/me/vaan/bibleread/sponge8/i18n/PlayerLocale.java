package me.vaan.bibleread.sponge8.i18n;

import me.vaan.bibleread.api.file.translation.LocaleHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.entity.living.player.Player;

public class PlayerLocale extends LocaleHolder {
    public PlayerLocale(Player player) {
        super(player.locale(), (msg) -> sendMessage(player, msg));
    }

    public static void sendMessage(Player player, String message) {
        Component cmp = PlainTextComponentSerializer.plainText().deserialize(message);
        player.sendMessage(cmp);
    }
}
