package kr.ac.waltdev29.oboetoki;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.DuplicateCheckResponse;
import kr.ac.waltdev29.oboetoki.data.model.User;
import kr.ac.waltdev29.oboetoki.databinding.ActivitySignupBinding;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private PreferenceManager preferenceManager;
    private boolean isEmailAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        binding.btnCheckEmail.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            checkEmail(email);
        });

        binding.btnSignupComplete.setOnClickListener(v -> performSignup());
    }

    private void checkEmail(String email) {
        RetrofitClient.getAuthService(preferenceManager)
                .checkEmail(email)
                .enqueue(new Callback<DuplicateCheckResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DuplicateCheckResponse> call, @NonNull Response<DuplicateCheckResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            isEmailAvailable = response.body().isAvailable;
                            if (isEmailAvailable) {
                                Toast.makeText(SignupActivity.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignupActivity.this, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "중복 확인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DuplicateCheckResponse> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(SignupActivity.this, "중복 확인 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performSignup() {
        if (!isEmailAvailable) {
            Toast.makeText(this, "이메일 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etPasswordConfirm.getText().toString().trim();
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);
        request.put("name", name);
        request.put("phone", phone);

        RetrofitClient.getAuthService(preferenceManager)
                .signup(request)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(SignupActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(SignupActivity.this, "회원가입 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
