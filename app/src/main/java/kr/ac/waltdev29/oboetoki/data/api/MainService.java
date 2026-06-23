package kr.ac.waltdev29.oboetoki.data.api;

import kr.ac.waltdev29.oboetoki.data.model.MainData;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MainService {
    @GET("/main/")
    Call<MainData> getMainData();
}
