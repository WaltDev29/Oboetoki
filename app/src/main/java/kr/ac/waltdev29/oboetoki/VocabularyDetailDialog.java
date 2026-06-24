package kr.ac.waltdev29.oboetoki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import kr.ac.waltdev29.oboetoki.data.model.Word;
import kr.ac.waltdev29.oboetoki.databinding.DialogVocabularyDetailBinding;
import kr.ac.waltdev29.oboetoki.util.ConfirmDialog;

public class VocabularyDetailDialog extends BottomSheetDialogFragment {

    private DialogVocabularyDetailBinding binding;
    private Word word;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (word == null) {
            dismiss();
            return;
        }

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> dismiss());

        binding.tvOriginalWord.setText(word.originalWord != null ? word.originalWord : "-");
        binding.tvReading.setText(word.reading != null && !word.reading.isEmpty() ? word.reading : "-");
        binding.tvTranslatedWord.setText(word.translatedWord != null ? word.translatedWord : "-");
        binding.tvCreatedAt.setText(word.createdAt != null ? word.createdAt.split("T")[0] : "-");

        if (word.isMemorized) {
            binding.tvMemorizedStatus.setText("완료");
            binding.tvMemorizedStatus.setTextColor(getResources().getColor(R.color.color_0058be, null));
            // Should add a background drawable for completed status, but sticking to text color change for simplicity
        } else {
            binding.tvMemorizedStatus.setText("미완료");
        }

        binding.btnDelete.setOnClickListener(v -> {
            ConfirmDialog confirmDialog = ConfirmDialog.newInstance("단어 삭제", "정말 이 단어를 단어장에서 삭제하시겠습니까?");
            confirmDialog.setOnConfirmListener(() -> {
                Toast.makeText(requireContext(), "단어 삭제 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show();
                // TODO: Call API to delete word
            });
            confirmDialog.show(getChildFragmentManager(), "DeleteConfirm");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
