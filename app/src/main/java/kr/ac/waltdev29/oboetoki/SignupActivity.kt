package kr.ac.waltdev29.oboetoki

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient
import kr.ac.waltdev29.oboetoki.databinding.ActivitySignupBinding
import kr.ac.waltdev29.oboetoki.util.PreferenceManager

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var preferenceManager: PreferenceManager
    private var isUsernameAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnCheckUsername.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkUsername(username)
        }

        binding.btnSignup.setOnClickListener {
            performSignup()
        }
    }

    private fun checkUsername(username: String) {
        val authService = RetrofitClient.getAuthService(preferenceManager)
        lifecycleScope.launch {
            try {
                val response = authService.checkUsername(username)
                isUsernameAvailable = response.isAvailable
                if (isUsernameAvailable) {
                    Toast.makeText(this@SignupActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignupActivity, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SignupActivity, "중복 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSignup() {
        if (!isUsernameAvailable) {
            Toast.makeText(this, "아이디 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etPasswordConfirm.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = hashMapOf(
            "username" to username,
            "password" to password,
            "name" to name,
            "phone" to phone,
            "email" to email
        )

        val authService = RetrofitClient.getAuthService(preferenceManager)
        lifecycleScope.launch {
            try {
                authService.signup(request)
                Toast.makeText(this@SignupActivity, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SignupActivity, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
