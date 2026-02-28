package com.example.pestdetector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tvResult: TextView
    private lateinit var tvActionPlan: TextView
    private lateinit var infoCard: CardView
    private var interpreter: Interpreter? = null

    private val labels by lazy {
        try {
            assets.open("labels.txt").bufferedReader().readLines().filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ініціалізація View
        imageView = findViewById(R.id.imageView)
        tvResult = findViewById(R.id.tvResult)
        tvActionPlan = findViewById(R.id.tvAdviceText)
        infoCard = findViewById(R.id.infoCard)
        val btnCapture = findViewById<ExtendedFloatingActionButton>(R.id.btnCapture)
        val btnLibrary = findViewById<View>(R.id.btnLibrary)
        val switchTheme = findViewById<SwitchMaterial>(R.id.switchTheme)

        // --- ЛОГІКА ТЕМИ ---
        // Перевіряємо поточну тему, щоб встановити положення повзунка
        switchTheme.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // --- ЛОГІКА БІБЛІОТЕКИ ---
        btnLibrary.setOnClickListener {
            val intent = Intent(this, LibraryActivity::class.java)
            startActivity(intent)
        }

        // Завантаження моделі
        try {
            val options = Interpreter.Options().setNumThreads(4)
            interpreter = Interpreter(loadModelFile(), options)
        } catch (e: Exception) {
            tvResult.text = "Модель не завантажено"
        }

        // Кнопка Камера
        btnCapture.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 100)
        }

        // Кнопка Галерея (натискання на рамку фото)
        findViewById<View>(R.id.cardView).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 200)
        }
    }

    // Решта методів (onActivityResult, processImage і т.д.) залишаються без змін
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                100 -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(bitmap)
                    processImage(bitmap)
                }
                200 -> {
                    val uri = data?.data
                    if (uri != null) {
                        val bitmap = loadFromUri(uri)
                        imageView.setImageBitmap(bitmap)
                        processImage(bitmap)
                    }
                }
            }
        }
    }

    private fun loadFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    private fun processImage(bitmap: Bitmap) {
        if (interpreter == null || labels.isEmpty()) return
        try {
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .build()

            var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), org.tensorflow.lite.DataType.FLOAT32)
            interpreter?.run(tensorImage.buffer, outputBuffer.buffer)

            val results = outputBuffer.floatArray
            val maxIndex = results.indices.maxByOrNull { results[it] } ?: -1

            if (maxIndex != -1 && results[maxIndex] > 0.35) {
                val labelName = labels[maxIndex]
                val confidence = (results[maxIndex] * 100).toInt()
                tvResult.text = "$labelName ($confidence%)"
                tvActionPlan.text = getActionPlan(labelName)
                infoCard.visibility = View.VISIBLE
            } else {
                tvResult.text = "Не розпізнано"
                infoCard.visibility = View.GONE
            }
        } catch (e: Exception) {
            tvResult.text = "Помилка аналізу"
        }
    }

    private fun getActionPlan(pest: String): String {
        return when (pest.lowercase().trim()) {
            "ants" -> "🐜 МУРАХИ: Використовуйте гелі-приманки або розчин борної кислоти з цукром."
            "bees" -> "🐝 БДЖОЛИ: Корисні комахи! Не знищуйте їх."
            "beetle" -> "🪲 ЖУКИ: Збирайте вручну вранці або обробіть біопрепаратами."
            "caterpillar" -> "🐛 ГУСЕНИЦІ: Настої гірчиці або господарського мила допоможуть захистити листя."
            "earthworms" -> "🪱 ХРОБАКИ: Дуже корисні для ґрунту! Не потребують боротьби."
            "earwig" -> "🦂 ЩИПАВКИ: Прибирайте вологе сміття. Зробіть пастки з вологих газет."
            "grasshopper" -> "🦗 САРАНА: Накривайте цінні рослини сіткою."
            "moth" -> "🦋 МОЛЬ: Використовуйте феромонні пастки та провітрюйте приміщення."
            "slug" -> "🐌 СЛИМАКИ: Встановіть пивні пастки або посипте землю попелом."
            "snail" -> "🐚 РАВЛИКИ: Видаляйте вручну ввечері. Мульчуйте землю сухою хвоєю."
            else -> "Спробуйте зробити фото ближче."
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = assets.openFd("pest_model.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        return inputStream.channel.map(FileChannel.MapMode.READ_ONLY, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
    }
}