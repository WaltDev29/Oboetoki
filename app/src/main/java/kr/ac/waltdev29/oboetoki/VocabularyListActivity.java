package kr.ac.waltdev29.oboetoki;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String sourceLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent() != null && getIntent().hasExtra("source_language")) {
            sourceLanguage = getIntent().getStringExtra("source_language");
        }

        setupRecyclerView();
        setupSearchDebounce();

        setupBottomNavigation(binding.includedBottomNav.bottomNavView, R.id.nav_vocabulary);
        fetchWords(null, "desc");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.hasExtra("source_language")) {
            sourceLanguage = intent.getStringExtra("source_language");
        }
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
        binding.includedBottomNav.bottomNavView.getMenu().findItem(R.id.nav_vocabulary).setChecked(true);
        fetchWords(null, "desc");
        
        binding.btnFilter.setOnClickListener(v -> {
            VocabularyFilterBottomSheet filterSheet = new VocabularyFilterBottomSheet();
            filterSheet.setOnFilterSelectedListener((isMemorized, sortOrder) -> {
                fetchWords(isMemorized, sortOrder);
            });
            filterSheet.show(getSupportFragmentManager(), "VocabularyFilter");
        });

        binding.tvHeaderOriginal.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            adapter.setOriginalMasked(!adapter.isOriginalMasked());
        });

        binding.tvHeaderReading.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            adapter.setReadingMasked(!adapter.isReadingMasked());
        });

        binding.tvHeaderTranslated.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            adapter.setTranslatedMasked(!adapter.isTranslatedMasked());
        });
    }

    private void fetchWords(Boolean isMemorized, String sortOrder) {
        RetrofitClient.getWordService(basePreferenceManager)
                .getWords(isMemorized, sourceLanguage, sortOrder)
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

    private void setupSearchDebounce() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        fetchWords(null, "desc");
                    } else {
                        searchWords(query);
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500); // 500ms debounce
            }
        });
    }

    private void searchWords(String query) {
        RetrofitClient.getWordService(basePreferenceManager)
                .searchWords(query, sourceLanguage)
                .enqueue(new Callback<List<Word>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Word>> call, @NonNull Response<List<Word>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateData(response.body());
                        } else {
                            NotificationDialog.newInstance("오류", "검색 결과를 불러오는데 실패했습니다.").show(getSupportFragmentManager(), "SearchWordsFail");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Word>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        NotificationDialog.newInstance("오류", "검색 결과를 불러오는데 실패했습니다:\n" + t.getMessage()).show(getSupportFragmentManager(), "SearchWordsFail");
                    }
                });
    }
}
