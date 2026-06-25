package kr.ac.waltdev29.oboetoki;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import kr.ac.waltdev29.oboetoki.data.model.Word;
import kr.ac.waltdev29.oboetoki.databinding.ItemVocabularyBinding;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Word word);
    }

    private List<Word> words;
    private final OnItemClickListener onItemClick;

    private boolean isOriginalMasked = false;
    private boolean isReadingMasked = false;
    private boolean isTranslatedMasked = false;

    private java.util.Set<Integer> peekedOriginals = new java.util.HashSet<>();
    private java.util.Set<Integer> peekedReadings = new java.util.HashSet<>();
    private java.util.Set<Integer> peekedTranslations = new java.util.HashSet<>();

    public VocabularyAdapter(List<Word> words, OnItemClickListener onItemClick) {
        this.words = words;
        this.onItemClick = onItemClick;
    }

    public void setOriginalMasked(boolean masked) {
        this.isOriginalMasked = masked;
        peekedOriginals.clear();
        notifyDataSetChanged();
    }
    public boolean isOriginalMasked() { return isOriginalMasked; }

    public void setReadingMasked(boolean masked) {
        this.isReadingMasked = masked;
        peekedReadings.clear();
        notifyDataSetChanged();
    }
    public boolean isReadingMasked() { return isReadingMasked; }

    public void setTranslatedMasked(boolean masked) {
        this.isTranslatedMasked = masked;
        peekedTranslations.clear();
        notifyDataSetChanged();
    }
    public boolean isTranslatedMasked() { return isTranslatedMasked; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemVocabularyBinding binding;

        public ViewHolder(ItemVocabularyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabularyBinding binding = ItemVocabularyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    private void applyMasking(android.widget.TextView tv, boolean isMasked, int defaultColorId) {
        if (isMasked) {
            tv.setTextColor(android.graphics.Color.TRANSPARENT);
            tv.setBackgroundResource(R.drawable.bg_masked_text);
        } else {
            tv.setTextColor(androidx.core.content.ContextCompat.getColor(tv.getContext(), defaultColorId));
            tv.setBackground(null);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = words.get(position);
        
        holder.binding.tvOriginalWord.setText(word.originalWord);
        
        if (word.reading != null && !word.reading.isEmpty()) {
            holder.binding.tvReading.setText(word.reading);
        } else {
            holder.binding.tvReading.setText("-");
        }
        
        holder.binding.tvTranslatedWord.setText(word.translatedWord);
        
        boolean originalMasked = isOriginalMasked && !peekedOriginals.contains(word.id);
        applyMasking(holder.binding.tvOriginalWord, originalMasked, R.color.gray_900);
        
        boolean readingMasked = isReadingMasked && !peekedReadings.contains(word.id);
        applyMasking(holder.binding.tvReading, readingMasked, R.color.gray_600);
        
        boolean translatedMasked = isTranslatedMasked && !peekedTranslations.contains(word.id);
        applyMasking(holder.binding.tvTranslatedWord, translatedMasked, R.color.gray_600);
        
        holder.binding.getRoot().setOnClickListener(v -> onItemClick.onItemClick(word));

        holder.binding.tvOriginalWord.setOnClickListener(v -> {
            if (originalMasked) {
                peekedOriginals.add(word.id);
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                onItemClick.onItemClick(word);
            }
        });

        holder.binding.tvReading.setOnClickListener(v -> {
            if (readingMasked) {
                peekedReadings.add(word.id);
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                onItemClick.onItemClick(word);
            }
        });

        holder.binding.tvTranslatedWord.setOnClickListener(v -> {
            if (translatedMasked) {
                peekedTranslations.add(word.id);
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                onItemClick.onItemClick(word);
            }
        });
    }

    @Override
    public int getItemCount() {
        return words != null ? words.size() : 0;
    }

    public void updateData(List<Word> newWords) {
        this.words = newWords;
        notifyDataSetChanged();
    }
}
