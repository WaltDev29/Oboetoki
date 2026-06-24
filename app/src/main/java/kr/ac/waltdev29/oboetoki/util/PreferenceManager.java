package kr.ac.waltdev29.oboetoki.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class PreferenceManager {

    private SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "oboetoki_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String token) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString("auth_token", token).apply();
        }
    }

    public String getToken() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString("auth_token", null);
        }
        return null;
    }

    public void setAutoLogin(boolean isAutoLogin) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean("auto_login", isAutoLogin).apply();
        }
    }

    public boolean isAutoLogin() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean("auto_login", false);
        }
        return false;
    }

    public void clearToken() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove("auth_token").apply();
        }
    }
}
