package kr.ac.waltdev29.oboetoki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.Word;
import kr.ac.waltdev29.oboetoki.data.model.BatchWordResponse;
import kr.ac.waltdev29.oboetoki.databinding.ActivityVocabularyListBinding;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VocabularyListActivity extends BaseNavigationActivity {

    private ActivityVocabularyListBinding binding;
    private VocabularyAdapter adapter;
    private boolean isOcrMode = false;
    private List<Word> parsedWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();

        String ocrJson = getIntent().getStringExtra("ocr_words");
        if (ocrJson != null && !ocrJson.isEmpty()) {
            isOcrMode = true;
            Type type = new TypeToken<List<Word>>() {}.getType();
            parsedWords = new Gson().fromJson(ocrJson, type);
            adapter.updateData(parsedWords);

            binding.includedBottomNav.bottomNavView.setVisibility(View.GONE);
            binding.bottomLayout.setVisibility(View.VISIBLE);

            binding.btnSaveBatch.setOnClickListener(v -> saveWordsBatch());
            binding.btnCancel.setOnClickListener(v -> finish());
        } else {
            setupBottomNavigation(binding.includedBottomNav.bottomNavView, R.id.nav_vocabulary);
            fetchWords(null);
        }
    }

    private void setupRecyclerView() {
        adapter = new VocabularyAdapter(new ArrayList<>(), word -> {
            VocabularyDetailDialog dialog = VocabularyDetailDialog.newInstance(word);
            dialog.show(getSupportFragmentManager(), "VocabularyDetailDialog");
        });
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isOcrMode) {
            binding.includedBottomNav.bottomNavView.setSelectedItemId(R.id.nav_vocabulary);
            fetchWords(null);
        }
        
        binding.btnFilter.setOnClickListener(v -> {
            VocabularyFilterBottomSheet filterSheet = new VocabularyFilterBottomSheet();
            filterSheet.setOnFilterSelectedListener(isMemorized -> {
                fetchWords(isMemorized);
            });
            filterSheet.show(getSupportFragmentManager(), "VocabularyFilter");
        });
    }

    private void fetchWords(Boolean isMemorized) {
        RetrofitClient.getWordService(basePreferenceManager)
                .getWords(isMemorized)
                .enqueue(new Callback<List<Word>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Word>> call, @NonNull Response<List<Word>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateData(response.body());
                        } else {
                            NotificationDialog.newInstance("오류", "단어 목록을 불러오는데 실패했습니다.").show(getSupportFragmentManager(), "FetchWordsFail");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Word>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        NotificationDialog.newInstance("오류", "단어 목록을 불러오는데 실패했습니다:\n" + t.getMessage()).show(getSupportFragmentManager(), "FetchWordsFail");
                    }
                });
    }

    private void saveWordsBatch() {
        if (parsedWords.isEmpty()) return;

        RetrofitClient.getWordService(basePreferenceManager)
                .addWordsBatch(parsedWords)
                .enqueue(new Callback<BatchWordResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BatchWordResponse> call, @NonNull Response<BatchWordResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            BatchWordResponse batchResponse = response.body();
                            int addedCount = batchResponse.addedWords != null ? batchResponse.addedWords.size() : 0;
                            String msg = "단어 추가 완료 (" + addedCount + "개)";
                            
                            if (batchResponse.ignoredWords != null && !batchResponse.ignoredWords.isEmpty()) {
                                msg += "\n중복 제외: " + batchResponse.ignoredWords.size() + "개";
                            }
                            
                            NotificationDialog successDialog = NotificationDialog.newInstance("알림", msg);
                            successDialog.setOnDismissAction(() -> {
                                isOcrMode = false;
                                binding.bottomLayout.setVisibility(View.GONE);
                                binding.includedBottomNav.bottomNavView.setVisibility(View.VISIBLE);
                                setupBottomNavigation(binding.includedBottomNav.bottomNavView, R.id.nav_vocabulary);
                                fetchWords(null);
                            });
                            successDialog.show(getSupportFragmentManager(), "BatchSaveSuccess");
                        } else if (response.code() == 409) {
                            NotificationDialog.newInstance("알림", "이미 등록된 단어가 존재합니다.").show(getSupportFragmentManager(), "BatchSaveConflict");
                        } else {
                            NotificationDialog.newInstance("오류", "저장 실패 (" + response.code() + ")").show(getSupportFragmentManager(), "BatchSaveFail");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BatchWordResponse> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        NotificationDialog.newInstance("오류", "저장 실패: " + t.getMessage()).show(getSupportFragmentManager(), "BatchSaveFail");
                    }
                });
    }
}
