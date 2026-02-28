package com.example.pestdetector

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LibraryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        // Додаємо кнопку "Назад" у верхній заголовок програми
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Довідник шкідників"

        val tvLibrary = findViewById<TextView>(R.id.tvLibraryContent)

        // Перевірка, чи знайдено TextView, щоб програма не вилітала
        if (tvLibrary != null) {
            val info = StringBuilder()
            info.append("🐜 ANTS (Мурахи): Захищають попелиць. Боротьба: гелі-приманки.\n\n")
            info.append("🐝 BEES (Бджоли): Корисні запилювачі. Не знищувати!\n\n")
            info.append("🪲 BEETLE (Жуки): Поїдають листя. Боротьба: ручний збір або біопрепарати.\n\n")
            info.append("🐛 CATERPILLAR (Гусениці): Швидко знищують зелень. Боротьба: настій полину.\n\n")
            info.append("🪱 EARTHWORMS (Хробаки): Друзі саду. Покращують ґрунт.\n\n")
            info.append("🦂 EARWIG (Щипавки): Люблять вологу. Боротьба: пастки з газет.\n\n")
            info.append("🦗 GRASSHOPPER (Сарана): Масовий шкідник. Боротьба: сітки на рослини.\n\n")
            info.append("🦋 MOTH (Моль/Метелики): Шкодять личинки. Боротьба: феромонні пастки.\n\n")
            info.append("🐌 SLUG (Слимаки): Активні вночі. Боротьба: пивні пастки або попіл.\n\n")
            info.append("🐚 SNAIL (Равлики): Аналогічно слимакам. Боротьба: хвойна мульча.")

            tvLibrary.text = info.toString()
        }
    }

    // Обробка натискання кнопки "Назад"
    override fun onSupportNavigateUp(): Boolean {
        finish() // Закриває цю сторінку і повертає до головної
        return true
    }
}