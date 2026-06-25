package kr.ac.waltdev29.oboetoki.data.model;

import com.google.gson.annotations.SerializedName;

public class MainData {
    @SerializedName("consecutive_attendance")
    public int consecutiveAttendance;
    
    @SerializedName("total_words")
    public int totalWords;
    
    @SerializedName("memorized_words")
    public int memorizedWords;
    
    @SerializedName("quote_of_the_day")
    public Quote quoteOfTheDay;

    public static class Quote {
        @SerializedName("text")
        public String text;
        
        @SerializedName("author")
        public String author;
    }
}
