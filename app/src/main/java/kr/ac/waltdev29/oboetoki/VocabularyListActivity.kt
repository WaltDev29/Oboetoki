package kr.ac.waltdev29.oboetoki

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.view.View
import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient
import kr.ac.waltdev29.oboetoki.data.model.Word
import kr.ac.waltdev29.oboetoki.databinding.ActivityVocabularyListBinding
import kr.ac.waltdev29.oboetoki.util.PreferenceManager

class VocabularyListActivity : BaseNavigationActivity() {

    private lateinit var binding: ActivityVocabularyListBinding
    private lateinit var adapter: VocabularyAdapter
    private var isOcrMode = false
    private var parsedWords: List<Word> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVocabularyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        val ocrJson = intent.getStringExtra("ocr_words")
        if (!ocrJson.isNullOrEmpty()) {
            isOcrMode = true
            val type = object : TypeToken<List<Word>>() {}.type
            parsedWords = Gson().fromJson(ocrJson, type)
            adapter.updateData(parsedWords)

            binding.bottomNavigationView.visibility = View.GONE
            binding.bottomLayout.visibility = View.VISIBLE

            binding.btnSaveBatch.setOnClickListener {
                saveWordsBatch()
            }
            binding.btnCancel.setOnClickListener {
                finish()
            }
        } else {
            setupBottomNavigation(binding.bottomNavigationView, R.id.nav_vocabulary)
            fetchWords()
        }
    }

    private fun setupRecyclerView() {
        adapter = VocabularyAdapter(emptyList()) { word ->
            // Handle item click, show detail dialog
            Toast.makeText(this, "단어 상세: ${word.originalWord}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (!isOcrMode) {
            binding.bottomNavigationView.selectedItemId = R.id.nav_vocabulary
            fetchWords()
        }
    }

    private fun fetchWords() {
        val wordService = RetrofitClient.getWordService(basePreferenceManager)
        lifecycleScope.launch {
            try {
                val words = wordService.getWords()
                adapter.updateData(words)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@VocabularyListActivity, "단어 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveWordsBatch() {
        if (parsedWords.isEmpty()) return

        val wordService = RetrofitClient.getWordService(basePreferenceManager)
        lifecycleScope.launch {
            try {
                wordService.addWordsBatch(parsedWords)
                Toast.makeText(this@VocabularyListActivity, "단어 추가 완료", Toast.LENGTH_SHORT).show()
                // Return to normal mode
                isOcrMode = false
                binding.bottomLayout.visibility = View.GONE
                binding.bottomNavigationView.visibility = View.VISIBLE
                setupBottomNavigation(binding.bottomNavigationView, R.id.nav_vocabulary)
                fetchWords()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@VocabularyListActivity, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
