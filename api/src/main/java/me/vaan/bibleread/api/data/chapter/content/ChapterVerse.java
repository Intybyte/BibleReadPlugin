package me.vaan.bibleread.api.data.chapter.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import me.vaan.bibleread.api.data.chapter.content.support.FormattedText;
import me.vaan.bibleread.api.data.chapter.content.support.InlineHeading;
import me.vaan.bibleread.api.data.chapter.content.support.InlineLineBreak;
import me.vaan.bibleread.api.data.chapter.content.support.VerseFootnoteReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class ChapterVerse extends ChapterContent {
    private int number;
    // Can be String, FormattedText, InlineHeading, InlineLineBreak, VerseFootnoteReference
    @JsonAdapter(ContentAdapter.class)
    private List<Object> content;

    private transient List<String> parsed = null;

    @Override
    public List<String> getParsed() {
        if (parsed != null) {
            return parsed;
        }

        List<String> list = new ArrayList<>(content.size());
        for (Object o : content) {
            String string = Objects.toString(o);
            list.add(string);
        }

        list.add("\n");
        parsed = list;

        return parsed;
    }

    public static class ContentAdapter extends TypeAdapter<List<Object>> {
        private final Gson gson = new GsonBuilder()
            .create();

        @Override
        public void write(JsonWriter out, List<Object> value) throws IOException {
            out.beginArray();
            for (Object item : value) {
                if (item instanceof String) {
                    out.value((String) item);
                } else {
                    gson.toJson(item, item.getClass(), out);
                }
            }
            out.endArray();
        }

        @Override
        public List<Object> read(JsonReader in) {
            JsonArray array = JsonParser.parseReader(in).getAsJsonArray();
            List<Object> result = new ArrayList<>(array.size());

            for (JsonElement element : array) {
                // Handle string
                if (!element.isJsonObject() && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    result.add(element.getAsString());
                    continue;
                }

                if (!element.isJsonObject()) {
                    throw new JsonParseException("Unexpected element in content array: " + element);
                }

                JsonObject obj = element.getAsJsonObject();

                Object deserialized = tryDeserialize(obj);
                if (deserialized != null) {
                    result.add(deserialized);
                } else {
                    throw new JsonParseException("Unknown object type in content array: " + obj);
                }
            }

            return result;
        }

        private Object tryDeserialize(JsonObject obj) {

            if (obj.has("text")) {
                return gson.fromJson(obj, FormattedText.class);
            }

            if (obj.has("heading")) {
                return gson.fromJson(obj, InlineHeading.class);
            }

            if (obj.has("lineBreak")) {
                return gson.fromJson(obj, InlineLineBreak.class);
            }

            if (obj.has("noteId")) {
                return gson.fromJson(obj, VerseFootnoteReference.class);
            }

            return null;
        }
    }
}
