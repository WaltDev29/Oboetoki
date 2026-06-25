package kr.ac.waltdev29.oboetoki;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import kr.ac.waltdev29.oboetoki.data.api.RetrofitClient;
import kr.ac.waltdev29.oboetoki.data.model.OcrResult;
import kr.ac.waltdev29.oboetoki.util.NotificationDialog;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseNavigationActivity extends AppCompatActivity {

    protected PreferenceManager basePreferenceManager;
    private File imageFile = null;
    private ProgressDialog progressDialog = null;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (imageBitmap != null) {
                        saveBitmapToFile(imageBitmap);
                        extractWords();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        File file = new File(getCacheDir(), "gallery_image.jpg");
                        FileOutputStream outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        outputStream.close();
                        inputStream.close();
                        imageFile = file;
                        extractWords();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        basePreferenceManager = new PreferenceManager(this);
    }

    private long lastNavClickTime = 0;

    protected void setupBottomNavigation(BottomNavigationView bottomNavigationView, int currentItemId) {
        if (currentItemId != -1) {
            bottomNavigationView.getMenu().findItem(currentItemId).setChecked(true);
        } else {
            // Uncheck all items for MyPage
            int size = bottomNavigationView.getMenu().size();
            for (int i = 0; i < size; i++) {
                bottomNavigationView.getMenu().getItem(i).setCheckable(false);
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            long currentClickTime = android.os.SystemClock.uptimeMillis();
            if (currentClickTime - lastNavClickTime < 500) {
                return false;
            }
            lastNavClickTime = currentClickTime;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                if (currentItemId != R.id.nav_home) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return true;
            } else if (itemId == R.id.nav_camera) {
                showImageSelectionDialog();
                bottomNavigationView.post(() -> {
                    if (currentItemId != -1) {
                        bottomNavigationView.getMenu().findItem(currentItemId).setChecked(true);
                    }
                });
                return true;
            } else if (itemId == R.id.nav_vocabulary) {
                LanguageSelectDialog dialog = new LanguageSelectDialog();
                dialog.setOnLanguageSelectedListener(sourceLanguage -> {
                    Intent intent = new Intent(this, VocabularyListActivity.class);
                    intent.putExtra("source_language", sourceLanguage);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                });
                dialog.show(getSupportFragmentManager(), "LanguageSelectDialog");

                if (currentItemId == R.id.nav_vocabulary) {
                    return true;
                }
                return false;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupHeader();
    }

    private void setupHeader() {
        android.view.View btnMyPage = findViewById(R.id.btnMyPage);
        if (btnMyPage != null) {
            btnMyPage.setOnClickListener(v -> {
                if (!(this instanceof MyPageActivity)) {
                    Intent intent = new Intent(this, MyPageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }
    }

    private void showImageSelectionDialog() {
        String[] options = {"사진 촬영", "앨범에서 선택"};
        new AlertDialog.Builder(this)
                .setTitle("이미지 가져오기")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndLaunch();
                    } else if (which == 1) {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "camera_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            imageFile = file;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractWords() {
        if (imageFile == null) return;

        showProgress("이미지에서 단어를 추출 중입니다...");

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        RetrofitClient.getWordService(basePreferenceManager)
                .parseOcrWords(body)
                .enqueue(new Callback<OcrResult>() {
                    @Override
                    public void onResponse(@NonNull Call<OcrResult> call, @NonNull Response<OcrResult> response) {
                        hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            OcrResultDialog dialog = OcrResultDialog.newInstance(response.body().parsedWords);
                            dialog.show(getSupportFragmentManager(), "OcrResultDialog");
                        } else {
                            try {
                                String errorMsg = response.errorBody() != null ? response.errorBody().string() : "알 수 없는 에러";
                                NotificationDialog.newInstance("오류", "추출 실패 (" + response.code() + "):\n" + errorMsg).show(getSupportFragmentManager(), "ExtractFail");
                            } catch (Exception e) {
                                NotificationDialog.newInstance("오류", "추출 실패 (" + response.code() + ")").show(getSupportFragmentManager(), "ExtractFail");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<OcrResult> call, @NonNull Throwable t) {
                        hideProgress();
                        t.printStackTrace();
                        NotificationDialog.newInstance("오류", "추출 실패:\n" + t.getMessage()).show(getSupportFragmentManager(), "ExtractFail");
                    }
                });
    }

    private void showProgress(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
