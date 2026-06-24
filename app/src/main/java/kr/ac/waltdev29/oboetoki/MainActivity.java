package kr.ac.waltdev29.oboetoki;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.MainData;
import kr.ac.waltdev29.oboetoki.databinding.ActivityMainBinding;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseNavigationActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation(binding.includedBottomNav.bottomNavView, R.id.nav_home);
        fetchMainData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.includedBottomNav.bottomNavView.setSelectedItemId(R.id.nav_home);
        fetchMainData(); // Refresh data on resume
    }

    private void fetchMainData() {
        RetrofitClient.getMainService(basePreferenceManager)
                .getMainData()
                .enqueue(new Callback<MainData>() {
                    @Override
                    public void onResponse(@NonNull Call<MainData> call, @NonNull Response<MainData> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            MainData data = response.body();
                            binding.tvConsecutiveAttendance.setText(String.valueOf(data.consecutiveAttendance));
                            binding.tvTotalWords.setText(String.valueOf(data.totalWords));
                            binding.tvMemorizedWords.setText(String.valueOf(data.memorizedWords));
                            binding.tvQuote.setText("\"" + data.quoteOfTheDay + "\"");

                            int percent = 0;
                            if (data.totalWords > 0) {
                                percent = (int) (((float) data.memorizedWords / data.totalWords) * 100);
                            }
                            binding.progressWords.setProgressCompat(percent, true);
                            binding.tvProgressPercent.setText(percent + "%");
                        } else {
                            NotificationDialog.newInstance("오류", "데이터를 불러오는데 실패했습니다.").show(getSupportFragmentManager(), "MainDataFail");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MainData> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        NotificationDialog.newInstance("오류", "데이터를 불러오는데 실패했습니다:\n" + t.getMessage()).show(getSupportFragmentManager(), "MainDataFail");
                    }
                });
    }
}
