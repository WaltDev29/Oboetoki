package kr.ac.waltdev29.oboetoki;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.TokenResponse;
import kr.ac.waltdev29.oboetoki.databinding.ActivityLoginBinding;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(email, password);
        });

        binding.btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin(String email, String password) {
        RetrofitClient.getAuthService(preferenceManager)
                .login(email, password)
                .enqueue(new Callback<TokenResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().accessToken;
                            if (binding.cbAutoLogin.isChecked()) {
                                preferenceManager.saveToken(token);
                            } else {
                                preferenceManager.saveToken(token);
                            }

                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(LoginActivity.this, "로그인 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
