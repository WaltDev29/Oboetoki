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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();

        setupBottomNavigation(binding.includedBottomNav.bottomNavView, R.id.nav_vocabulary);
        fetchWords(null, "desc");
    }

    private void setupRecyclerView() {
        adapter = new VocabularyAdapter(new ArrayList<>(), word -> {
            VocabularyDetailDialog dialog = VocabularyDetailDialog.newInstance(word);
            dialog.setOnWordChangedListener(() -> {
                fetchWords(null, "desc");
            });
            dialog.show(getSupportFragmentManager(), "VocabularyDetailDialog");
        });
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.includedBottomNav.bottomNavView.setSelectedItemId(R.id.nav_vocabulary);
        fetchWords(null, "desc");
        
        binding.btnFilter.setOnClickListener(v -> {
            VocabularyFilterBottomSheet filterSheet = new VocabularyFilterBottomSheet();
            filterSheet.setOnFilterSelectedListener((isMemorized, sortOrder) -> {
                fetchWords(isMemorized, sortOrder);
            });
            filterSheet.show(getSupportFragmentManager(), "VocabularyFilter");
        });
    }

    private void fetchWords(Boolean isMemorized, String sortOrder) {
        RetrofitClient.getWordService(basePreferenceManager)
                .getWords(isMemorized, sortOrder)
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
}
