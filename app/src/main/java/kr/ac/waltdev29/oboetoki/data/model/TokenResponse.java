package kr.ac.waltdev29.oboetoki.data.model;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName("access_token")
    public String accessToken;
    
    @SerializedName("token_type")
    public String tokenType;
}
