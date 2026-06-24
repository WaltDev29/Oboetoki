package kr.ac.waltdev29.oboetoki;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.ac.waltdev29.oboetoki.data.model.Word;
import kr.ac.waltdev29.oboetoki.databinding.ItemOcrWordBinding;

public class OcrResultAdapter extends RecyclerView.Adapter<OcrResultAdapter.ViewHolder> {

    public interface OnWordRemovedListener {
        void onWordRemoved(int newCount);
    }

    private final List<Word> words;
    private final OnWordRemovedListener listener;

    public OcrResultAdapter(List<Word> words, OnWordRemovedListener listener) {
        this.words = words;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemOcrWordBinding binding;

        public ViewHolder(ItemOcrWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOcrWordBinding binding = ItemOcrWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        ViewHolder holder = new ViewHolder(binding);

        // 실시간 텍스트 반영
        holder.binding.etOriginalWord.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    words.get(pos).originalWord = s.toString().trim();
                }
            }
        });

        holder.binding.etReading.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    words.get(pos).reading = s.toString().trim();
                }
            }
        });

        holder.binding.etMeaning.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    words.get(pos).translatedWord = s.toString().trim();
                }
            }
        });

        // 삭제 버튼 이벤트
        holder.binding.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                words.remove(pos);
                notifyItemRemoved(pos);
                if (listener != null) {
                    listener.onWordRemoved(words.size());
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = words.get(position);
        
        // 텍스트 설정 (TextWatcher가 트리거되지만 데이터가 같거나 다듬어져서 큰 문제 없음)
        holder.binding.etOriginalWord.setText(word.originalWord != null ? word.originalWord : "");
        holder.binding.etReading.setText(word.reading != null ? word.reading : "");
        holder.binding.etMeaning.setText(word.translatedWord != null ? word.translatedWord : "");
    }

    @Override
    public int getItemCount() {
        return words != null ? words.size() : 0;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
