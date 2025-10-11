package me.vaan.bibleread.api.data.translation;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
public class Translation {

    private String id;
    private String name;
    private String englishName;
    private String website;
    private String licenseUrl;
    private String shortName;
    private String language;

    @SerializedName("languageName")
    private String languageName;

    @SerializedName("languageEnglishName")
    private String languageEnglishName;

    @SerializedName("textDirection")
    private TextDirection textDirection;

    @SerializedName("availableFormats")
    private Set<AvailableFormat> availableFormats;

    @SerializedName("listOfBooksApiLink")
    private String listOfBooksApiLink;

    @SerializedName("numberOfBooks")
    private int numberOfBooks;

    @SerializedName("totalNumberOfChapters")
    private int totalNumberOfChapters;

    @SerializedName("totalNumberOfVerses")
    private int totalNumberOfVerses;

    @SerializedName("numberOfApocryphalBooks")
    private Integer numberOfApocryphalBooks;

    @SerializedName("totalNumberOfApocryphalChapters")
    private Integer totalNumberOfApocryphalChapters;

    @SerializedName("totalNumberOfApocryphalVerses")
    private Integer totalNumberOfApocryphalVerses;
}
