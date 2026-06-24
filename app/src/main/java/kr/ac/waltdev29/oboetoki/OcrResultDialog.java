package kr.ac.waltdev29.oboetoki;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.BatchWordResponse;
import kr.ac.waltdev29.oboetoki.data.model.Word;
import kr.ac.waltdev29.oboetoki.databinding.DialogOcrResultBinding;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OcrResultDialog extends DialogFragment {

    private DialogOcrResultBinding binding;
    private List<Word> words;
    private OcrResultAdapter adapter;
    private ProgressDialog progressDialog;

    public static OcrResultDialog newInstance(List<Word> words) {
        OcrResultDialog dialog = new OcrResultDialog();
        Bundle args = new Bundle();
        args.putString("words_json", new Gson().toJson(words));
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString("words_json");
            Type type = new TypeToken<List<Word>>() {}.getType();
            words = new Gson().fromJson(json, type);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogOcrResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (words == null || words.isEmpty()) {
            dismiss();
            return;
        }

        binding.tvTitle.setText("추출된 단어 (" + words.size() + "개)");

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OcrResultAdapter(words, newCount -> {
            binding.tvTitle.setText("추출된 단어 (" + newCount + "개)");
        });
        binding.recyclerView.setAdapter(adapter);

        binding.btnSaveAll.setOnClickListener(v -> saveWordsBatch());
    }

    private void saveWordsBatch() {
        showProgress("단어를 저장 중입니다...");
        PreferenceManager pref = new PreferenceManager(requireContext());
        RetrofitClient.getWordService(pref).addWordsBatch(words).enqueue(new Callback<BatchWordResponse>() {
            @Override
            public void onResponse(@NonNull Call<BatchWordResponse> call, @NonNull Response<BatchWordResponse> response) {
                hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    BatchWordResponse batchResponse = response.body();
                    int addedCount = batchResponse.addedWords != null ? batchResponse.addedWords.size() : 0;
                    String msg = "단어 추가 완료: " + addedCount + "개";
                    
                    if (batchResponse.ignoredWords != null && !batchResponse.ignoredWords.isEmpty()) {
                        msg += "\n중복 제외: " + batchResponse.ignoredWords.size() + "개";
                    }

                    NotificationDialog successDialog = NotificationDialog.newInstance("저장 완료", msg);
                    successDialog.setOnDismissAction(() -> {
                        dismiss();
                        Intent intent = new Intent(requireContext(), VocabularyListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    });
                    successDialog.show(getChildFragmentManager(), "BatchSaveSuccess");
                } else {
                    NotificationDialog.newInstance("저장 실패", "저장 중 오류가 발생했습니다.").show(getChildFragmentManager(), "BatchSaveFail");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BatchWordResponse> call, @NonNull Throwable t) {
                hideProgress();
                NotificationDialog.newInstance("저장 실패", "네트워크 오류가 발생했습니다.").show(getChildFragmentManager(), "BatchSaveFail");
            }
        });
    }

    private void showProgress(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
