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
import kr.ac.waltdev29.oboetoki.util.ConfirmDialog;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private PreferenceManager preferenceManager;
    private boolean isEmailAvailable = false;
    private boolean isPasswordVisible = false;
    private boolean isPasswordConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnCheckEmail.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "유효한 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            checkEmail(email);
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

        binding.btnTogglePasswordConfirm.setOnClickListener(v -> {
            isPasswordConfirmVisible = !isPasswordConfirmVisible;
            if (isPasswordConfirmVisible) {
                binding.etPasswordConfirm.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.btnTogglePasswordConfirm.setImageResource(R.drawable.ic_eye);
            } else {
                binding.etPasswordConfirm.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnTogglePasswordConfirm.setImageResource(R.drawable.ic_eye_off);
            }
            binding.etPasswordConfirm.setSelection(binding.etPasswordConfirm.getText().length());
        });

        binding.etPhone.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String digits = s.toString().replaceAll("\\D", "");
                StringBuilder formatted = new StringBuilder();
                if (digits.length() > 3) {
                    formatted.append(digits.substring(0, 3)).append("-");
                    if (digits.length() > 7) {
                        formatted.append(digits.substring(3, 7)).append("-");
                        formatted.append(digits.substring(7));
                    } else {
                        formatted.append(digits.substring(3));
                    }
                } else {
                    formatted.append(digits);
                }
                s.replace(0, s.length(), formatted.toString());
                isFormatting = false;
            }
        });

        binding.btnSignupComplete.setOnClickListener(v -> attemptSignup());
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
                                NotificationDialog.newInstance("알림", "사용 가능한 이메일입니다.").show(getSupportFragmentManager(), "EmailCheck");
                            } else {
                                NotificationDialog.newInstance("알림", "이미 사용 중인 이메일입니다.").show(getSupportFragmentManager(), "EmailCheck");
                            }
                        } else {
                            NotificationDialog.newInstance("오류", "중복 확인에 실패했습니다.").show(getSupportFragmentManager(), "EmailCheck");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DuplicateCheckResponse> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        NotificationDialog.newInstance("오류", "중복 확인 실패: " + t.getMessage()).show(getSupportFragmentManager(), "EmailCheck");
                    }
                });
    }

    private void attemptSignup() {
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

        String passwordPattern = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,12}$";
        if (!password.matches(passwordPattern)) {
            Toast.makeText(this, "비밀번호는 8자 이상 12자 이하이며 특수문자를 1개 이상 포함해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "유효한 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String phonePattern = "^01([0|1|6|7|8|9])-?([0-9]{3,4})-?([0-9]{4})$";
        if (!phone.matches(phonePattern)) {
            Toast.makeText(this, "유효한 전화번호 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        ConfirmDialog confirmDialog = ConfirmDialog.newInstance("회원가입", "입력하신 정보로 회원가입을 진행하시겠습니까?");
        confirmDialog.setOnConfirmListener(() -> executeSignup(email, password, name, phone));
        confirmDialog.show(getSupportFragmentManager(), "SignupConfirm");
    }

    private void executeSignup(String email, String password, String name, String phone) {
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
                            NotificationDialog successDialog = NotificationDialog.newInstance("알림", "회원가입이 완료되었습니다.");
                            successDialog.setOnDismissAction(() -> finish());
                            successDialog.show(getSupportFragmentManager(), "SignupSuccess");
                        } else {
                            NotificationDialog.newInstance("오류", "회원가입에 실패했습니다.").show(getSupportFragmentManager(), "SignupFail");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        NotificationDialog.newInstance("오류", "회원가입 실패: " + t.getMessage()).show(getSupportFragmentManager(), "SignupFail");
                    }
                });
    }
}
