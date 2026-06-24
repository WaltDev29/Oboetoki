package kr.ac.waltdev29.oboetoki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.DialogFragment;
import com.google.gson.Gson;

import java.util.HashMap;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.Word;
import kr.ac.waltdev29.oboetoki.databinding.DialogVocabularyDetailBinding;
import kr.ac.waltdev29.oboetoki.util.ConfirmDialog;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import kr.ac.waltdev29.oboetoki.util.ConfirmDialog;

public class VocabularyDetailDialog extends DialogFragment {

    public interface OnWordChangedListener {
        void onWordChanged();
    }

    private DialogVocabularyDetailBinding binding;
    private Word word;
    private boolean isEdited = false;
    private OnWordChangedListener listener;

    public void setOnWordChangedListener(OnWordChangedListener listener) {
        this.listener = listener;
    }

    public static VocabularyDetailDialog newInstance(Word word) {
        VocabularyDetailDialog dialog = new VocabularyDetailDialog();
        Bundle args = new Bundle();
        args.putString("word_json", new Gson().toJson(word));
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString("word_json");
            word = new Gson().fromJson(json, Word.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogVocabularyDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (word == null) {
            dismiss();
            return;
        }

        binding.btnClose.setOnClickListener(v -> dismiss());

        binding.etOriginalWord.setText(word.originalWord != null ? word.originalWord : "");
        binding.etReading.setText(word.reading != null ? word.reading : "");
        binding.etMeaning.setText(word.translatedWord != null ? word.translatedWord : "");
        binding.tvCreatedAt.setText(word.createdAt != null ? word.createdAt.split("T")[0] : "-");

        updateMemorizedStatusUI();

        binding.tvMemorizedStatus.setOnClickListener(v -> {
            word.isMemorized = !word.isMemorized;
            updateMemorizedStatusUI();
            updateWordMemorizedStatus();
        });

        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                isEdited = true;
                binding.btnConfirm.setText("수정");
            }
        };

        binding.etOriginalWord.addTextChangedListener(textWatcher);
        binding.etReading.addTextChangedListener(textWatcher);
        binding.etMeaning.addTextChangedListener(textWatcher);

        binding.btnConfirm.setOnClickListener(v -> {
            if (isEdited) {
                updateWordDetails();
            } else {
                dismiss();
            }
        });

        binding.btnDelete.setOnClickListener(v -> {
            ConfirmDialog confirmDialog = ConfirmDialog.newInstance("단어 삭제", "정말 이 단어를 단어장에서 삭제하시겠습니까?");
            confirmDialog.setOnConfirmListener(() -> {
                deleteWord();
            });
            confirmDialog.show(getChildFragmentManager(), "DeleteConfirm");
        });
    }

    private void updateWordDetails() {
        HashMap<String, Object> req = new HashMap<>();
        req.put("original_word", binding.etOriginalWord.getText().toString().trim());
        req.put("reading", binding.etReading.getText().toString().trim());
        req.put("translated_word", binding.etMeaning.getText().toString().trim());
        req.put("is_memorized", word.isMemorized);

        PreferenceManager pref = new PreferenceManager(requireContext());
        RetrofitClient.getWordService(pref).updateWord(word.id, req).enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                if (response.isSuccessful()) {
                    NotificationDialog dialog = NotificationDialog.newInstance("알림", "단어가 수정되었습니다.");
                    dialog.setOnDismissAction(() -> {
                        if (listener != null) listener.onWordChanged();
                        dismiss();
                    });
                    dialog.show(getChildFragmentManager(), "UpdateSuccess");
                } else {
                    NotificationDialog.newInstance("오류", "단어 수정 실패").show(getChildFragmentManager(), "UpdateFail");
                }
            }
            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                NotificationDialog.newInstance("오류", "단어 수정 실패: " + t.getMessage()).show(getChildFragmentManager(), "UpdateFail");
            }
        });
    }

    private void updateMemorizedStatusUI() {
        if (word.isMemorized) {
            binding.tvMemorizedStatus.setText("완료");
            binding.tvMemorizedStatus.setTextColor(getResources().getColor(R.color.color_ffffff, null));
            binding.tvMemorizedStatus.setBackgroundResource(R.drawable.bg_badge_completed);
        } else {
            binding.tvMemorizedStatus.setText("미완료");
            binding.tvMemorizedStatus.setTextColor(getResources().getColor(R.color.color_ff85b2, null));
            binding.tvMemorizedStatus.setBackgroundResource(R.drawable.bg_badge_primary);
        }
    }

    private void updateWordMemorizedStatus() {
        HashMap<String, Object> req = new HashMap<>();
        req.put("original_word", word.originalWord != null ? word.originalWord : "");
        req.put("reading", word.reading != null ? word.reading : "");
        req.put("translated_word", word.translatedWord != null ? word.translatedWord : "");
        req.put("is_memorized", word.isMemorized);

        PreferenceManager pref = new PreferenceManager(requireContext());
        RetrofitClient.getWordService(pref).updateWord(word.id, req).enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                if (response.isSuccessful()) {
                    if (listener != null) listener.onWordChanged();
                } else {
                    // API 실패 시 롤백
                    word.isMemorized = !word.isMemorized;
                    updateMemorizedStatusUI();
                    Toast.makeText(requireContext(), "상태 변경 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                // API 실패 시 롤백
                word.isMemorized = !word.isMemorized;
                updateMemorizedStatusUI();
                Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWord() {
        PreferenceManager pref = new PreferenceManager(requireContext());
        RetrofitClient.getWordService(pref).deleteWord(word.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    NotificationDialog dialog = NotificationDialog.newInstance("알림", "단어가 삭제되었습니다.");
                    dialog.setOnDismissAction(() -> {
                        if (listener != null) listener.onWordChanged();
                        dismiss();
                    });
                    dialog.show(getChildFragmentManager(), "DeleteSuccess");
                } else {
                    NotificationDialog.newInstance("오류", "단어 삭제 실패").show(getChildFragmentManager(), "DeleteFail");
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                NotificationDialog.newInstance("오류", "단어 삭제 실패: " + t.getMessage()).show(getChildFragmentManager(), "DeleteFail");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
