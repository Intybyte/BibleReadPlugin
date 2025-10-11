package me.vaan.bibleread.api.data.book;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class TranslationBook {
    /**
     * The ID of the book.
     */
    @SerializedName("id")
    private String id;

    /**
     * The name that the translation provided for the book.
     */
    @SerializedName("name")
    private String name;

    /**
     * The common name for the book.
     */
    @SerializedName("commonName")
    private String commonName;

    /**
     * The title of the book. Nullable.
     */
    @SerializedName("title")
    private String title; // Nullable, so no primitive

    /**
     * The numerical order of the book in the translation.
     */
    @SerializedName("order")
    private int order;

    /**
     * The number of chapters that the book contains.
     */
    @SerializedName("numberOfChapters")
    private int numberOfChapters;

    /**
     * The link to the first chapter of the book.
     */
    @SerializedName("firstChapterApiLink")
    private String firstChapterApiLink;

    /**
     * The link to the last chapter of the book.
     */
    @SerializedName("lastChapterApiLink")
    private String lastChapterApiLink;

    /**
     * The number of verses that the book contains.
     */
    @SerializedName("totalNumberOfVerses")
    private int totalNumberOfVerses;

    /**
     * Whether the book is an apocryphal book. Nullable (optional in JSON).
     */
    @SerializedName("isApocryphal")
    private Boolean isApocryphal;
}
