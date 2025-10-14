package me.vaan.bibleread.bukkit.parser;

import me.vaan.bibleread.api.parser.BookParser;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

public class BukkitBookParser implements BookParser {

    @Override
    public int getCharWidth(char c) {
        MapFont.CharacterSprite sprite = MinecraftFont.Font.getChar(c);
        if (sprite == null) return 6;

        return sprite.getWidth();
    }
}
