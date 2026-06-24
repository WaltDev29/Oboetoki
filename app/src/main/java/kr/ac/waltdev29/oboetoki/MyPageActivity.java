package kr.ac.waltdev29.oboetoki;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.User;
import kr.ac.waltdev29.oboetoki.databinding.ActivityMyPageBinding;
import kr.ac.waltdev29.oboetoki.util.ConfirmDialog;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageActivity extends BaseNavigationActivity {

    private ActivityMyPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation(binding.includedBottomNav.bottomNavView, -1);

        fetchUserProfile();
        setupClickListeners();
    }

    private void fetchUserProfile() {
        RetrofitClient.getAuthService(basePreferenceManager).getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    binding.tvUserName.setText(user.name != null ? user.name : "이름 없음");
                    binding.tvUserEmail.setText(user.email != null ? user.email : "이메일 정보 없음");
                } else {
                    binding.tvUserName.setText("알 수 없음");
                    binding.tvUserEmail.setText("사용자 정보를 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                t.printStackTrace();
                binding.tvUserName.setText("연결 오류");
                binding.tvUserEmail.setText("오프라인 상태이거나 서버 오류입니다.");
            }
        });
    }

    private void setupClickListeners() {
        binding.btnAppInfo.setOnClickListener(v -> showAppInfo());

        binding.btnLogout.setOnClickListener(v -> {
            ConfirmDialog dialog = ConfirmDialog.newInstance("로그아웃", "로그아웃 하시겠습니까?");
            dialog.setOnConfirmListener(() -> {
                basePreferenceManager.clearToken();
                basePreferenceManager.setAutoLogin(false);
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            dialog.show(getSupportFragmentManager(), "LogoutConfirm");
        });
    }

    private void showAppInfo() {
        String version = "1.0.0";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String infoMessage = "Oboetoki v" + version + "\n\n문의 : waltdev29@gmail.com";
        NotificationDialog.newInstance("앱 정보", infoMessage).show(getSupportFragmentManager(), "AppInfo");
    }
}
