package kr.ac.waltdev29.oboetoki;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.TokenResponse;
import kr.ac.waltdev29.oboetoki.databinding.ActivityLoginBinding;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;
    private boolean isPasswordVisible = false;

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

        binding.btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.btnTogglePassword.setImageResource(R.drawable.ic_eye);
            } else {
                binding.etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
            }
            binding.etPassword.setSelection(binding.etPassword.getText().length());
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
                            preferenceManager.saveToken(token);
                            preferenceManager.setAutoLogin(binding.cbAutoLogin.isChecked());

                            NotificationDialog dialog = NotificationDialog.newInstance("알림", "로그인에 성공했습니다.");
                            dialog.setOnDismissAction(() -> {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                            dialog.show(getSupportFragmentManager(), "LoginSuccess");
                        } else {
                            NotificationDialog.newInstance("알림", "로그인에 실패했습니다.").show(getSupportFragmentManager(), "LoginFail");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        NotificationDialog.newInstance("오류", "네트워크 오류가 발생했습니다.\n" + t.getMessage()).show(getSupportFragmentManager(), "LoginFail");
                    }
                });
    }
}
