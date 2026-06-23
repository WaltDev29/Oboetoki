package kr.ac.waltdev29.oboetoki.data.api;

import java.util.HashMap;

import kr.ac.waltdev29.oboetoki.data.model.DuplicateCheckResponse;
import kr.ac.waltdev29.oboetoki.data.model.TokenResponse;
import kr.ac.waltdev29.oboetoki.data.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthService {
    @POST("/auth/signup")
    Call<User> signup(@Body HashMap<String, String> request);

    @GET("/auth/check-email")
    Call<DuplicateCheckResponse> checkEmail(@Query("email") String email);

    @FormUrlEncoded
    @POST("/auth/login")
    Call<TokenResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );
}
