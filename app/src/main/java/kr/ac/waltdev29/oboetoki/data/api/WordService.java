package kr.ac.waltdev29.oboetoki.data.api;

import java.util.HashMap;
import java.util.List;

import kr.ac.waltdev29.oboetoki.data.model.OcrResult;
import kr.ac.waltdev29.oboetoki.data.model.Word;
import kr.ac.waltdev29.oboetoki.data.model.BatchWordResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WordService {
    @GET("words/")
    Call<List<Word>> getWords(
            @Query("is_memorized") Boolean isMemorized
    );

    @POST("words/")
    Call<Word> addWord(@Body Word request);

    @POST("words/batch")
    Call<BatchWordResponse> addWordsBatch(@Body List<Word> request);

    @GET("words/{word_id}")
    Call<Word> getWordDetail(@Path("word_id") int wordId);

    @PUT("words/{word_id}")
    Call<Word> updateWord(
            @Path("word_id") int wordId,
            @Body HashMap<String, Object> request
    );

    @Multipart
    @POST("words/ocr")
    Call<OcrResult> parseOcrWords(@Part MultipartBody.Part file);
}
