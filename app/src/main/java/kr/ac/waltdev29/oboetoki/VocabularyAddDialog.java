package kr.ac.waltdev29.oboetoki;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.Word;
import kr.ac.waltdev29.oboetoki.databinding.DialogVocabularyDetailBinding;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VocabularyAddDialog extends DialogFragment {

    private DialogVocabularyDetailBinding binding;
    private String sourceLanguage = "en";
    private Runnable onWordAddedListener;
    private PreferenceManager preferenceManager;

    public static VocabularyAddDialog newInstance(String sourceLanguage) {
        VocabularyAddDialog dialog = new VocabularyAddDialog();
        Bundle args = new Bundle();
        args.putString("source_language", sourceLanguage);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnWordAddedListener(Runnable listener) {
        this.onWordAddedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sourceLanguage = getArguments().getString("source_language", "en");
        }
        preferenceManager = new PreferenceManager(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().density * 320),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogVocabularyDetailBinding.inflate(inflater, container, false);

        // UI Setup
        binding.tvTitle.setText("단어 등록");
        binding.btnDelete.setVisibility(View.GONE);
        binding.layoutCreatedAt.setVisibility(View.GONE);
        binding.layoutMemorizedStatus.setVisibility(View.GONE);
        binding.layoutLanguage.setVisibility(View.VISIBLE);

        binding.btnEditOriginalWord.setVisibility(View.GONE);
        binding.btnEditReading.setVisibility(View.GONE);
        binding.btnEditMeaning.setVisibility(View.GONE);

        // Make fields editable
        setupEditableField(binding.etOriginalWord, "단어 입력");
        setupEditableField(binding.etReading, "발음 입력 (선택)");
        setupEditableField(binding.etMeaning, "뜻 입력 (선택)");

        // Set Language text
        updateLanguageText();

        binding.btnClose.setOnClickListener(v -> dismiss());

        binding.tvLanguageSelect.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), binding.tvLanguageSelect);
            popupMenu.getMenu().add(0, 0, 0, "영어");
            popupMenu.getMenu().add(0, 1, 1, "일본어");
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sourceLanguage = "en";
                } else {
                    sourceLanguage = "ja";
                }
                updateLanguageText();
                return true;
            });
            popupMenu.show();
        });

        binding.btnConfirm.setOnClickListener(v -> addWord());

        return binding.getRoot();
    }

    private void setupEditableField(android.widget.EditText editText, String hint) {
        editText.setText("");
        editText.setHint(hint);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setCursorVisible(true);
    }

    private void updateLanguageText() {
        if ("ja".equals(sourceLanguage)) {
            binding.tvLanguageSelect.setText("일본어");
        } else {
            binding.tvLanguageSelect.setText("영어");
        }
    }

    private void addWord() {
        String original = binding.etOriginalWord.getText().toString().trim();
        String reading = binding.etReading.getText().toString().trim();
        String meaning = binding.etMeaning.getText().toString().trim();

        if (original.isEmpty()) {
            Toast.makeText(requireContext(), "원어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Word word = new Word();
        word.originalWord = original;
        word.reading = reading;
        word.translatedWord = meaning;
        word.sourceLanguage = sourceLanguage;

        RetrofitClient.getWordService(preferenceManager).addWord(word).enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                if (response.isSuccessful()) {
                    if (onWordAddedListener != null) {
                        onWordAddedListener.run();
                    }
                    dismiss();
                } else if (response.code() == 409) {
                    NotificationDialog.newInstance("중복", "이미 단어장에 등록된 단어입니다.").show(getParentFragmentManager(), "DuplicateWord");
                } else {
                    Toast.makeText(requireContext(), "단어 등록 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
