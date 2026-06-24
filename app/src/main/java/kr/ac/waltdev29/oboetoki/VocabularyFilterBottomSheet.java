package kr.ac.waltdev29.oboetoki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import kr.ac.waltdev29.oboetoki.databinding.DialogVocabularyFilterBinding;

public class VocabularyFilterBottomSheet extends BottomSheetDialogFragment {

    public interface OnFilterSelectedListener {
        void onFilterSelected(Boolean isMemorized, String sortOrder);
    }

    private DialogVocabularyFilterBinding binding;
    private OnFilterSelectedListener listener;

    public void setOnFilterSelectedListener(OnFilterSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogVocabularyFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnClose.setOnClickListener(v -> dismiss());

        binding.btnFilterAll.setOnClickListener(v -> {
            if (listener != null) listener.onFilterSelected(null, "desc");
            dismiss();
        });

        binding.btnFilterNotMemorized.setOnClickListener(v -> {
            if (listener != null) listener.onFilterSelected(false, "desc");
            dismiss();
        });

        binding.btnFilterMemorized.setOnClickListener(v -> {
            if (listener != null) listener.onFilterSelected(true, "desc");
            dismiss();
        });

        binding.btnFilterRecent.setOnClickListener(v -> {
            if (listener != null) listener.onFilterSelected(null, "desc");
            dismiss();
        });

        binding.btnFilterOldest.setOnClickListener(v -> {
            if (listener != null) listener.onFilterSelected(null, "asc");
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
