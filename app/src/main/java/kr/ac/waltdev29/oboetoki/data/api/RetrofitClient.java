package kr.ac.waltdev29.oboetoki.data.api;

import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String BASE_URL = kr.ac.waltdev29.oboetoki.BuildConfig.BASE_URL;
    private static Retrofit retrofit = null;

    public static Retrofit getClient(PreferenceManager preferenceManager) {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            AuthInterceptor authInterceptor = new AuthInterceptor(preferenceManager);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(authInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthService getAuthService(PreferenceManager preferenceManager) {
        return getClient(preferenceManager).create(AuthService.class);
    }

    public static MainService getMainService(PreferenceManager preferenceManager) {
        return getClient(preferenceManager).create(MainService.class);
    }

    public static WordService getWordService(PreferenceManager preferenceManager) {
        return getClient(preferenceManager).create(WordService.class);
    }
}
