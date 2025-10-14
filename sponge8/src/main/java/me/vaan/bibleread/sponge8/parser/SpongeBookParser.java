package me.vaan.bibleread.sponge8.parser;

import me.vaan.bibleread.api.parser.BookParser;
import me.vaan.bibleread.sponge8.parser.fontmap.MapFont;
import me.vaan.bibleread.sponge8.parser.fontmap.MinecraftFont;

public class SpongeBookParser implements BookParser {
    @Override
    public int getCharWidth(char c) {
        MapFont.CharacterSprite sprite = MinecraftFont.Font.getChar(c);
        if (sprite == null) return 6;

        return sprite.getWidth();
    }
}
