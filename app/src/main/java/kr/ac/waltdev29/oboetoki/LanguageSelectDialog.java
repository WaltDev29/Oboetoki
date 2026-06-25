package kr.ac.waltdev29.oboetoki;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class LanguageSelectDialog extends DialogFragment {

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String sourceLanguage);
    }

    private OnLanguageSelectedListener listener;

    public void setOnLanguageSelectedListener(OnLanguageSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_language_select, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().density * 320);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnEnglish = view.findViewById(R.id.btnEnglish);
        Button btnJapanese = view.findViewById(R.id.btnJapanese);
        TextView btnCancel = view.findViewById(R.id.btnCancel);

        btnEnglish.setOnClickListener(v -> {
            if (listener != null) listener.onLanguageSelected("en");
            dismiss();
        });

        btnJapanese.setOnClickListener(v -> {
            if (listener != null) listener.onLanguageSelected("ja");
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
