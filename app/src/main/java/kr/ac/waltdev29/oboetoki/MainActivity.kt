package kr.ac.waltdev29.oboetoki

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient
import kr.ac.waltdev29.oboetoki.databinding.ActivityMainBinding
import kr.ac.waltdev29.oboetoki.util.PreferenceManager

class MainActivity : BaseNavigationActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation(binding.bottomNavigationView, R.id.nav_home)
        fetchMainData()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationView.selectedItemId = R.id.nav_home
        fetchMainData() // Refresh data on resume
    }

    private fun fetchMainData() {
        val mainService = RetrofitClient.getMainService(basePreferenceManager)
        lifecycleScope.launch {
            try {
                val data = mainService.getMainData()
                binding.tvConsecutiveAttendance.text = data.consecutiveAttendance.toString()
                binding.tvTotalWords.text = data.totalWords.toString()
                binding.tvMemorizedWords.text = data.memorizedWords.toString()
                binding.tvQuote.text = "\"${data.quoteOfTheDay}\""
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
