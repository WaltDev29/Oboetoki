package kr.ac.waltdev29.oboetoki.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OcrResult {
    @SerializedName("parsed_words")
    public List<Word> parsedWords;
}
