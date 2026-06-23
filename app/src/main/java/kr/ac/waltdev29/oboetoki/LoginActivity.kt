package kr.ac.waltdev29.oboetoki

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient
import kr.ac.waltdev29.oboetoki.databinding.ActivityLoginBinding
import kr.ac.waltdev29.oboetoki.util.PreferenceManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(username: String, password: String) {
        val authService = RetrofitClient.getAuthService(preferenceManager)
        lifecycleScope.launch {
            try {
                // Call API
                val response = authService.login(username, password)
                
                // Save token if auto login is checked, or just save it for the session
                // For simplicity, we save it here. You can add logic for auto-login checkbox.
                if (binding.cbAutoLogin.isChecked) {
                    preferenceManager.saveToken(response.accessToken)
                } else {
                    preferenceManager.saveToken(response.accessToken) // or save to session-only storage
                }

                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, "로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
