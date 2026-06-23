package kr.ac.waltdev29.oboetoki

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient
import kr.ac.waltdev29.oboetoki.util.PreferenceManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

abstract class BaseNavigationActivity : AppCompatActivity() {

    protected lateinit var basePreferenceManager: PreferenceManager
    private var imageFile: File? = null
    private var progressDialog: ProgressDialog? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            if (imageBitmap != null) {
                saveBitmapToFile(imageBitmap)
                extractWords()
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "gallery_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            imageFile = file
            extractWords()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        basePreferenceManager = PreferenceManager(this)
    }

    protected fun setupBottomNavigation(bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        bottomNavigationView.selectedItemId = currentItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentItemId != R.id.nav_home) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        startActivity(intent)
                    }
                    true
                }
                R.id.nav_camera -> {
                    showImageSelectionDialog()
                    // Revert selection so it doesn't look stuck on camera
                    bottomNavigationView.post {
                        bottomNavigationView.selectedItemId = currentItemId
                    }
                    true
                }
                R.id.nav_vocabulary -> {
                    if (currentItemId != R.id.nav_vocabulary) {
                        val intent = Intent(this, VocabularyListActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("사진 촬영", "앨범에서 선택")
        AlertDialog.Builder(this)
            .setTitle("이미지 가져오기")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun saveBitmapToFile(bitmap: Bitmap) {
        val file = File(cacheDir, "camera_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        imageFile = file
    }

    private fun extractWords() {
        if (imageFile == null) return

        showProgress("이미지에서 단어를 추출 중입니다...")

        val requestFile = imageFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", imageFile!!.name, requestFile)

        val wordService = RetrofitClient.getWordService(basePreferenceManager)
        lifecycleScope.launch {
            try {
                val response = wordService.parseOcrWords(body)
                val parsedWords = response.parsedWords
                
                // 직렬화하여 VocabularyListActivity로 전달
                val jsonWords = Gson().toJson(parsedWords)
                val intent = Intent(this@BaseNavigationActivity, VocabularyListActivity::class.java)
                intent.putExtra("ocr_words", jsonWords)
                startActivity(intent)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@BaseNavigationActivity, "추출 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                hideProgress()
            }
        }
    }

    private fun showProgress(message: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this).apply {
                setCancelable(false)
            }
        }
        progressDialog?.setMessage(message)
        progressDialog?.show()
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }
}
