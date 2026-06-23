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
    private var isEmailAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        // Removed btnCancel listener as the button was removed in the layout update

        binding.btnCheckEmail.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkEmail(email)
        }

        binding.btnSignupComplete.setOnClickListener {
            performSignup()
        }
    }

    private fun checkEmail(email: String) {
        val authService = RetrofitClient.getAuthService(preferenceManager)
        lifecycleScope.launch {
            try {
                val response = authService.checkEmail(email)
                isEmailAvailable = response.isAvailable
                if (isEmailAvailable) {
                    Toast.makeText(this@SignupActivity, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignupActivity, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SignupActivity, "중복 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSignup() {
        if (!isEmailAvailable) {
            Toast.makeText(this, "이메일 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etPasswordConfirm.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = hashMapOf(
            "email" to email,
            "password" to password,
            "name" to name,
            "phone" to phone
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
