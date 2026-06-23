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

    public VocabularyAdapter(List<Word> words, OnItemClickListener onItemClick) {
        this.words = words;
        this.onItemClick = onItemClick;
    }

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = words.get(position);
        
        String originalText;
        if (word.reading != null && !word.reading.isEmpty()) {
            originalText = word.originalWord + " (" + word.reading + ")";
        } else {
            originalText = word.originalWord;
        }
        
        holder.binding.tvOriginalWord.setText(originalText);
        holder.binding.tvTranslatedWord.setText(word.translatedWord);
        
        holder.binding.getRoot().setOnClickListener(v -> onItemClick.onItemClick(word));
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
