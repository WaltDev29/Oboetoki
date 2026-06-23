package kr.ac.waltdev29.oboetoki.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    public int id;
    public String email;
    public String name;
    public String phone;
    
    @SerializedName("consecutive_attendance")
    public int consecutiveAttendance;
    
    @SerializedName("last_login_date")
    public String lastLoginDate;
}
