package kr.ac.waltdev29.oboetoki.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BatchWordResponse {
    @SerializedName("added_words")
    public List<Word> addedWords;
    
    @SerializedName("ignored_words")
    public List<String> ignoredWords;
}
