package kr.ac.waltdev29.oboetoki.data.model;

import com.google.gson.annotations.SerializedName;

public class Word {
    public int id = 0;
    
    @SerializedName("user_id")
    public int userId = 0;
    
    @SerializedName("original_word")
    public String originalWord;
    
    public String reading;
    
    @SerializedName("translated_word")
    public String translatedWord;
    
    @SerializedName("source_language")
    public String sourceLanguage;
    
    @SerializedName("is_memorized")
    public boolean isMemorized = false;
    
    @SerializedName("created_at")
    public String createdAt;
}
