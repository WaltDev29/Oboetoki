package kr.ac.waltdev29.oboetoki.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("name")
    public String name;

    @SerializedName("email")
    public String email;
}
