package kr.ac.waltdev29.oboetoki

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.ac.waltdev29.oboetoki.data.model.Word
import kr.ac.waltdev29.oboetoki.databinding.ItemVocabularyBinding

class VocabularyAdapter(
    private var words: List<Word>,
    private val onItemClick: (Word) -> Unit
) : RecyclerView.Adapter<VocabularyAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemVocabularyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVocabularyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = words[position]
        holder.binding.tvOriginalWord.text = word.originalWord
        holder.binding.tvTranslatedWord.text = word.translatedWord
        
        holder.binding.root.setOnClickListener {
            onItemClick(word)
        }
    }

    override fun getItemCount() = words.size

    fun updateData(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }
}
