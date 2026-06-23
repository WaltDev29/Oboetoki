package kr.ac.waltdev29.oboetoki

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient
import kr.ac.waltdev29.oboetoki.data.model.Word
import kr.ac.waltdev29.oboetoki.databinding.ActivityVocabularyListBinding
import kr.ac.waltdev29.oboetoki.util.PreferenceManager

class VocabularyListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVocabularyListBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var adapter: VocabularyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVocabularyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        setupRecyclerView()
        setupBottomNavigation()
        fetchWords()
    }

    private fun setupRecyclerView() {
        adapter = VocabularyAdapter(emptyList()) { word ->
            // Handle item click, show detail dialog
            Toast.makeText(this, "단어 상세: ${word.originalWord}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.nav_vocabulary

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                R.id.nav_camera -> {
                    val intent = Intent(this, CameraActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                R.id.nav_vocabulary -> {
                    // Already here
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationView.selectedItemId = R.id.nav_vocabulary
        fetchWords()
    }

    private fun fetchWords() {
        val wordService = RetrofitClient.getWordService(preferenceManager)
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
}
