package me.vaan.bibleread.api.data.chapter.content;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;

import java.io.IOException;
import java.util.Locale;

@Getter
@JsonAdapter(ChapterContent.Adapter.class)
public abstract class ChapterContent implements ParseList {
    private ChapterContentType type;

    public static class Adapter extends TypeAdapter<ChapterContent> {
        private final Gson gson = new Gson();

        @Override
        public void write(JsonWriter out, ChapterContent value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            JsonObject jsonObject = gson.toJsonTree(value, value.getClass()).getAsJsonObject();
            gson.toJson(jsonObject, out);
        }

        @Override
        public ChapterContent read(JsonReader in) throws IOException {
            JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();

            JsonElement typeElement = jsonObject.get("type");
            if (typeElement == null) {
                throw new JsonParseException("Missing 'type' field in ChapterContent JSON");
            }

            String typeString = typeElement.getAsString();
            ChapterContentType type;

            try {
                type = ChapterContentType.valueOf(typeString.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Unknown ChapterContent type: " + typeString);
            }

            return gson.fromJson(jsonObject, type.getChapterClass());
        }
    }
}
