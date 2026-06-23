package kr.ac.waltdev29.oboetoki

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient
import kr.ac.waltdev29.oboetoki.data.model.Word
import kr.ac.waltdev29.oboetoki.databinding.ActivityCameraBinding
import kr.ac.waltdev29.oboetoki.util.PreferenceManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var adapter: VocabularyAdapter
    private var selectedImageUri: Uri? = null
    private var imageFile: File? = null
    private var parsedWords: List<Word> = emptyList()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            if (imageBitmap != null) {
                binding.ivPreview.setImageBitmap(imageBitmap)
                saveBitmapToFile(imageBitmap)
                binding.btnExtract.isEnabled = true
            }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
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

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivPreview.setImageURI(uri)
            
            // Convert URI to File
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "gallery_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            imageFile = file
            
            binding.btnExtract.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        setupRecyclerView()

        binding.btnCamera.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnExtract.setOnClickListener {
            extractWords()
        }

        binding.btnSaveBatch.setOnClickListener {
            saveWordsBatch()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = VocabularyAdapter(emptyList()) { word ->
            // Let user edit the word (omitted for brevity, can show dialog)
            Toast.makeText(this, "단어 수정 (미구현): ${word.originalWord}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.adapter = adapter
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

        binding.progressBar.visibility = View.VISIBLE
        binding.btnExtract.isEnabled = false

        val requestFile = imageFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", imageFile!!.name, requestFile)

        val wordService = RetrofitClient.getWordService(preferenceManager)
        lifecycleScope.launch {
            try {
                val response = wordService.parseOcrWords(body)
                parsedWords = response.parsedWords
                adapter.updateData(parsedWords)
                
                binding.bottomLayout.visibility = View.VISIBLE
                Toast.makeText(this@CameraActivity, "추출 성공", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CameraActivity, "추출 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnExtract.isEnabled = true
            }
        }
    }

    private fun saveWordsBatch() {
        if (parsedWords.isEmpty()) return

        binding.progressBar.visibility = View.VISIBLE
        
        val wordService = RetrofitClient.getWordService(preferenceManager)
        lifecycleScope.launch {
            try {
                wordService.addWordsBatch(parsedWords)
                Toast.makeText(this@CameraActivity, "단어 추가 완료", Toast.LENGTH_SHORT).show()
                finish() // Or go to vocabulary list
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CameraActivity, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
